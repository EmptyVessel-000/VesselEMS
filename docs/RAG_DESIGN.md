# RAG 知识库 — 完整设计方案

> 更新时间：2026-06-12

---

## 一、架构总览 — 三层模型

```
┌─────────────────────────────────────────────────────────────┐
│  Layer 1 — 文件系统（组织层）                                 │
│                                                             │
│  library = 目录，document = 文件                              │
│  用户自由创建目录、上传文件、删除、编辑                         │
│  这一层只管"在哪里、叫什么"                                   │
├─────────────────────────────────────────────────────────────┤
│  Layer 2 — 标注层（系统自动处理）                              │
│                                                             │
│  系统根据 file_type 自动分流处理：                            │
│    txt/pdf/docx/md → Tika解析 → 切片 → embedding → 入库      │
│    jpg/png/gif     → Base64 → Vision模型 → 分析结果入库       │
│                                                             │
│  所有标注结果统一存入 annotation 表                           │
├─────────────────────────────────────────────────────────────┤
│  Layer 3 — 消费层（大模型使用）                               │
│                                                             │
│  知识问答：用户问题 → embed → 向量检索 → 找到相关标注 → LLM   │
│  文件详情：展开查看标注内容（切片文本 / 图片分析结果）          │
└─────────────────────────────────────────────────────────────┘
```

---

## 二、前置依赖

### 2.1 Maven 依赖（`pom.xml` 新增）

```xml
<!-- pgvector: PostgreSQL 向量扩展 JDBC 驱动 -->
<dependency>
    <groupId>com.pgvector</groupId>
    <artifactId>pgvector</artifactId>
    <version>0.1.6</version>
</dependency>

<!-- Apache Tika: 内存流直接解析文档（PDF/Word/Markdown/TXT → 纯文本） -->
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>3.1.0</version>
</dependency>
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-parsers-standard-package</artifactId>
    <version>3.1.0</version>
</dependency>
```

### 2.2 基础设施

| 依赖 | 状态 | 说明 |
|------|------|------|
| PostgreSQL + pgvector 扩展 | ✅ 已就绪 | `CREATE EXTENSION vector;` |
| text-embedding-v4 API | 外部 | `/v1/embeddings`，输出 1536 维向量 |
| qwen3.5-35b-a3b / d-flash API | 外部 | `/chat/completions` (vision)，图片标注 |
| RAG 问答模型 API | 外部 | `/chat/completions`，生成答案 |

### 2.3 Spring Boot 已有依赖（无需新增）

`spring-boot-starter-webmvc` / `spring-boot-starter-data-jpa` / `spring-boot-starter-security` / PostgreSQL JDBC Driver

---

## 三、数据库设计（4 张表 + model 表 2 个新字段）

### 3.1 修改现有表

**model 表** — 新增字段：

```sql
ALTER TABLE model ADD COLUMN version INT DEFAULT 0;
ALTER TABLE model ADD COLUMN model_type VARCHAR(32) DEFAULT NULL;
```

| 字段 | 类型 | 取值 | 说明 |
|------|------|------|------|
| `version` | INT | 0 / 1 | 0=NL2SQL模块, 1=RAG模块 |
| `model_type` | VARCHAR(32) | NULL / CHAT / EMBEDDING / VISION | 当 version=1 时必填，区分用途 |

model 表典型数据：

| id | name | modelId | version | model_type | 用途 |
|----|------|---------|---------|-----------|------|
| 1 | GPT-4o | gpt-4o | 0 | NULL | NL2SQL |
| 2 | GPT-4o | gpt-4o | 1 | CHAT | RAG 问答 |
| 3 | text-embedding-v4 | text-embedding-v4 | 1 | EMBEDDING | 向量化 |
| 4 | qwen3.5-35b | qwen3.5-35b-a3b | 1 | VISION | 图片标注 |
| 5 | d-flash | d-flash | 1 | VISION | 图片标注备选 |

### 3.2 新增 DDL

```sql
-- 1. 目录（文库）
CREATE TABLE library (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    status INT DEFAULT 1,
    create_time TIMESTAMP NOT NULL
);

-- 2. 文件（统一入口，所有类型都在此）
CREATE TABLE document (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    library_id BIGINT NOT NULL REFERENCES library(id),
    file_name VARCHAR(256) NOT NULL,
    file_type VARCHAR(32) NOT NULL,
    file_size BIGINT NOT NULL,
    status INT DEFAULT 0,
    create_time TIMESTAMP NOT NULL
);
-- status: 0=排队中, 1=处理中, 2=已完成, -1=失败

-- 3. 标注结果（文本切片 + 图片分析，统一存储）
CREATE TABLE annotation (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES document(id),
    annotation_index INT,              -- 切片序号（文本文件有，图片为NULL）
    content TEXT NOT NULL,             -- 切片文本 / 图片分析结果
    vector_id VARCHAR(128),            -- 向量关联（图片为NULL）
    model_id BIGINT REFERENCES model(id),
    create_time TIMESTAMP NOT NULL
);

-- 4. 向量存储 + IVFFlat 索引
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE vector_embedding (
    id VARCHAR(128) PRIMARY KEY,
    annotation_id BIGINT NOT NULL,
    embedding vector(1536) NOT NULL
);

CREATE INDEX ON vector_embedding
    USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
```

### 3.3 表关系

```
library (1) ────< (N) document (1) ────< (N) annotation (1) ──── (1) vector_embedding
                                        │
                                        ├── annotation_index = 0,1,2... (文本切片)
                                        │       content = 切片文本
                                        │       vector_id → vector_embedding
                                        │
                                        └── annotation_index = NULL (图片)
                                                content = Vision 分析结果
                                                vector_id = NULL
```

### 3.4 级联删除

| 操作 | 级联 |
|------|------|
| 删除 library | → 删该库所有 document → 删所有 annotation → 删所有 vector_embedding |
| 删除 document | → 删所有 annotation → 删所有 vector_embedding |

---

## 四、数据流

### 4.1 文件上传 → 自动标注（不存磁盘，byte[] 传入异步）

```
用户上传文件到文库
  │
  ↓ POST /api/library/{libId}/document (multipart)
  │
  ├── Controller 将 MultipartFile 读为 byte[]
  ├── INSERT document (status=0)
  └── 返回 {id, status:0}
  │
  │  @Async 异步处理 (传入 docId + byte[])
  ↓
  ├── document.status → 1

  ├── file_type 分流:
  │
  │   ┌─ txt/pdf/docx/md ────────────────────────────┐
  │   │ Tika 解析 (ByteArrayInputStream) → 纯文本      │
  │   │ 滑动窗口切片 (512字符 + 50重叠)                  │
  │   │                                              │
  │   │ for each 切片:                                │
  │   │   → embed(切片文本) → float[1536]              │
  │   │   → INSERT annotation (content=切片文本,       │
  │   │       annotation_index=i, model_id=embedding) │
  │   │   → INSERT vector_embedding                   │
  │   │   → UPDATE annotation.vector_id               │
  │   └──────────────────────────────────────────────┘
  │
  │   ┌─ jpg/png/gif ────────────────────────────────┐
  │   │ Base64 编码（从 byte[]） → Vision API           │
  │   │   → 分析结果文本                                │
  │   │   → INSERT annotation (content=分析结果,        │
  │   │       annotation_index=NULL, model_id=vision)  │
  │   │   (无向量，不插入 vector_embedding)             │
  │   └──────────────────────────────────────────────┘
  │
  └── document.status → 2 (完成)
      异常: status → -1
```

### 4.2 知识问答

```
用户选择文库 + 输入问题 + 选择模型 (model_type=CHAT)
  │
  ↓ POST /api/rag/query
  │
  ├── EmbeddingService.embed(问题) → 问题向量
  │
  ├── VectorStoreService.search(问题向量, libraryId, topK=5)
  │     一条 SQL JOIN 检索
  │
  ├── 拼接提示词（系统提示 + 检索到的标注内容 + 用户问题）
  ├── LLMService.chat(model, prompt) → answer
  └── 返回 {answer, references: [{annotationId, content, score}]}
```

---

## 五、后端服务（Tools）

| 服务 | 职责 | 核心方法 |
|------|------|----------|
| **LLMService** (修改) | OpenAI 兼容 API 统一调用 | `chat()` (已有), `embedding()` (新增), `chatVision()` (新增) |
| **EmbeddingService** | 查 model_type=EMBEDDING 的模型 → 调 API | `embed(text) → float[1536]` |
| **VectorStoreService** | pgvector 向量存储与检索（JdbcTemplate） | `insert(annotationId, float[])`, `search(float[], libId, topK)`, `deleteByDocId()` |
| **DocumentChunkingService** | Tika 解析 + 滑动窗口切片 | `chunk(InputStream, fileType) → List<String>` |
| **DocumentService** | 上传 + @Async 统一标注（文本/图片分流） | `upload(MultipartFile, libId)`, `processAsync(docId, byte[])`, `delete(docId)` |
| **LibraryService** | 文库 CRUD + 级联删除 | `create()`, `update()`, `delete()` |
| **RAGService** | 知识问答 | `query(libId, question, modelId)` |

### LLMService 新增方法

```java
// 文本向量化 → POST {apiUrl}/v1/embeddings
public float[] embedding(Model model, String text);

// 多模态图片对话 → POST {apiUrl}/chat/completions
// messages content: [{type:"text", text:prompt},
//   {type:"image_url", image_url:{url:"data:image/jpeg;base64,..."}}]
public String chatVision(Model model, List<String> base64Images, String prompt);
```

### VectorStoreService 检索 SQL

```sql
SELECT a.id, a.content,
       1 - (v.embedding <=> ?::vector) AS similarity
FROM vector_embedding v
JOIN annotation a ON a.id = v.annotation_id
JOIN document d ON d.id = a.document_id
WHERE d.library_id = ?
ORDER BY v.embedding <=> ?::vector
LIMIT ?
```

### 异步线程池

```java
@Configuration @EnableAsync
public class AsyncConfig {
    @Bean("documentProcessExecutor")
    public Executor executor() {
        ThreadPoolTaskExecutor e = new ThreadPoolTaskExecutor();
        e.setCorePoolSize(2);
        e.setMaxPoolSize(4);
        e.setQueueCapacity(100);
        e.setThreadNamePrefix("doc-process-");
        return e;
    }
}
```

---

## 六、后端 API（3 个 Controller，8 个端点）

### LibraryController — `/api/library`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/library` | 文库列表 |
| POST | `/api/library` | 新建 `{name, description}` |
| PUT | `/api/library/{id}` | 编辑 |
| DELETE | `/api/library/{id}` | 级联删除 |

### DocumentController — `/api/library/{libId}/document`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/library/{libId}/document` | 文件列表（附带标注数量和状态） |
| POST | `/api/library/{libId}/document` | 上传文件 (multipart) → `{id, status:0}` |
| DELETE | `/api/library/{libId}/document/{docId}` | 删除（级联删 annotation + 向量） |

### RAGController — `/api/rag`

| 方法 | 路径 | 请求体 | 响应体 |
|------|------|--------|--------|
| POST | `/api/rag/query` | `{libraryId, question, modelId}` | `{answer, references[{annotationId, content, score}]}` |

---

## 七、前端设计

### 7.1 页面清单（2 个页面）

| 页面 | 路由 | 菜单 ID | 说明 |
|------|------|---------|------|
| **RAGKnowledge** | `/workspace/rag-knowledge` | 31 | 飞书侧边栏风格，文库管理 + 文件管理 + 标注查看 |
| **RAGQuery** | `/workspace/rag-query` | 32 | 知识问答 |

### 7.2 RAGKnowledge 界面（飞书侧边栏风格）

```
┌──────────────────┬────────────────────────────────────────────┐
│  左侧侧边栏       │  右侧内容区                                  │
│                  │                                             │
│ [+ 新建文库]      │  📁 产品文库                                 │
│                  │  产品相关设计文档                             │
│ ─────────────────│─────────────────────────────────────────────│
│                  │                                             │
│ 📁 产品文库 ←选中 │  [📤 上传]  [🔄 刷新状态]                    │
│   15 个文件      │                                             │
│                  │  ┌────┬────────┬──────┬──────┬─────┬──────┐│
│ 📁 技术文库      │  │类型│  文件名  │ 大小 │标注数│ 状态 │操作  ││
│   3 个文件       │  │📄  │系统手册  │1.2MB│  12  │✓完成│🗑   ││
│                  │  │🖼  │架构图    │856KB│   1  │✓完成│🗑   ││
│ 📁 会议记录      │  │📄  │接口规范  │500KB│   0  │⏳处理│🗑  ││
│   8 个文件       │  └────┴────────┬──────┴──────┴─────┴──────┘│
│                  │             分页: [< 1 >]                   │
│                  │                                             │
│                  │  ┌─────────── 标注详情 ──────────────────┐  │
│                  │  │ 文本文件 → 显示切片列表                 │  │
│                  │  │ 图片文件 → 显示 Vision 分析结果         │  │
│                  │  └───────────────────────────────────────┘  │
└──────────────────┴────────────────────────────────────────────┘
```

**交互**：
- 左侧选中文库 → 右侧显示文件列表
- 文库悬停显示 ✏️ 编辑 / 🗑 删除
- 上传文件后系统自动标注，手动刷新查看状态
- 展开文件查看标注详情（文本切片 / 图片分析结果）

### 7.3 RAGQuery 界面

```
┌──────────────────────┬───────────────────────────────────────┐
│  配置区               │  对话区                                │
│  选择文库 [▼]         │                                       │
│  选择模型 [▼]         │  用户：介绍一下系统架构                 │
│  (仅显示model_type   │  AI：根据资料，VesselEMS 采用分层...   │
│   =CHAT的模型)       │       📎 引用 (相似度: 92%)            │
│                       │       📎 引用 (相似度: 87%)            │
│ ──────────────────────│                                       │
│  输入问题...     [发送]│                                       │
└──────────────────────┴───────────────────────────────────────┘
```

### 7.4 修改页面

| 页面 | 修改 |
|------|------|
| `workspace.vue` | RAG 子菜单改为 2 项（知识库、知识问答） |
| `Model.vue` | 表单加 `version`（`el-radio`: 0=NL2SQL, 1=RAG）+ `model_type`（`el-select`: CHAT/EMBEDDING/VISION），表格加 version/model_type 列 |
| `workspace.js` | 新增 2 条路由，删除旧 DocumentManage 路由 |

### 7.5 菜单结构

```html
<el-sub-menu index="rag" v-if="hasMenu(3)">
  <el-menu-item v-if="hasMenu(31)" index="/workspace/rag-knowledge">知识库</el-menu-item>
  <el-menu-item v-if="hasMenu(32)" index="/workspace/rag-query">知识问答</el-menu-item>
</el-sub-menu>
```

### 7.6 路由

```js
const RAGKnowledge = () => import('../views/workspace/RAGKnowledge.vue')
const RAGQuery = () => import('../views/workspace/RAGQuery.vue')

{ path: 'rag-knowledge', name: 'RAGKnowledge', component: RAGKnowledge, meta: { title: '知识库' } },
{ path: 'rag-query', name: 'RAGQuery', component: RAGQuery, meta: { title: '知识问答' } },
```

---

## 八、完整文件清单

```
后端新增 (20个):
├── model/Library.java, Document.java, Annotation.java
├── repository/LibraryRepository.java, DocumentRepository.java,
│            AnnotationRepository.java
├── service/EmbeddingService.java, VectorStoreService.java,
│          DocumentChunkingService.java, DocumentService.java,
│          LibraryService.java, RAGService.java
├── controller/LibraryController.java, DocumentController.java,
│             RAGController.java
├── config/RAGMenuInitializer.java, AsyncConfig.java
└── dto/RAGQueryRequest.java, RAGQueryResponse.java

后端修改 (4个):
├── pom.xml                           (+pgvector +tika)
├── model/Model.java                  (+version +model_type)
├── controller/ModelController.java   (+version +model_type in update)
└── service/LLMService.java           (+embedding +chatVision)

前端新增 (2个):
├── views/workspace/RAGKnowledge.vue   飞书侧边栏风格
└── views/workspace/RAGQuery.vue       知识问答

前端修改 (3个):
├── views/workspace.vue                (RAG菜单改为2项)
├── views/workspace/Model.vue          (+version +model_type)
└── router/workspace.js                (新增2条路由，删除旧DocumentManage路由)

前端删除 (1个):
└── views/workspace/DocumentManage.vue  (被 RAGKnowledge 替代)
```

---

## 九、关键设计决策

| 决策 | 选择 | 原因 |
|------|------|------|
| 文件存储 | **不存磁盘** | Tika 直接从内存 byte[] 解析，标注结果存 annotation 表 |
| 标注存储 | **统一 annotation 表** | 文本切片和图片分析结果归一化存储 |
| 模型分类 | **model_type 字段** | CHAT/EMBEDDING/VISION 三种类型，方便各 Service 精确查找 |
| 异步传参 | **byte[] 而非 InputStream** | 避免 @Async 中原始请求流关闭导致读取失败 |
| 向量索引 | **IVFFlat** | 万级数据足够，维护简单 |
| 向量库 | **pgvector** | 同库同连接，JOIN 检索一条 SQL |
| 版本标识 | **model.version INT 0/1** | 简洁明确 |
| 异步标注 | **@Async ThreadPool** | 上传即返回，后台处理 |
| 切片策略 | **512 字符 + 50 重叠** | 简单有效 |
| 检索 Top-K | **5** | 兼顾覆盖与 token 消耗 |
| 前端页面 | **2 页（知识库 + 知识问答）** | 合并文库管理、文件管理、多模态标注为一个飞书风格页面 |