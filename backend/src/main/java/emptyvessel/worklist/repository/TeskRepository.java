package emptyvessel.worklist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import emptyvessel.worklist.dto.TeskWithNamesDto;
import emptyvessel.worklist.model.Tesk;

@Repository
public interface TeskRepository extends JpaRepository<Tesk, Long> {

    /**
     * 获取所有任务，包含创建者和分配者的用户名
     * 使用 LEFT JOIN 确保即使没有分配者也能返回任务
     */
    @Query("SELECT new emptyvessel.worklist.dto.TeskWithNamesDto(" +
            "t.id, t.name, t.description, t.creatorUserId, cu.username, " +
            "t.assignmentUserId, au.username, t.startTime, t.endTime, " +
            "t.createdAt, t.status, t.assignedAt, t.submittedAt, t.completedAt, " +
            "t.rejectionCount, t.comment, t.contactEmail, t.isDeleted) " +
            "FROM Tesk t " +
            "LEFT JOIN User cu ON t.creatorUserId = cu.id " +
            "LEFT JOIN User au ON t.assignmentUserId = au.id " +
            "WHERE t.isDeleted = false")
    List<TeskWithNamesDto> findAllWithUserNames();

    /**
     * 获取分配给指定用户的任务，包含用户名
     */
    @Query("SELECT new emptyvessel.worklist.dto.TeskWithNamesDto(" +
            "t.id, t.name, t.description, t.creatorUserId, cu.username, " +
            "t.assignmentUserId, au.username, t.startTime, t.endTime, " +
            "t.createdAt, t.status, t.assignedAt, t.submittedAt, t.completedAt, " +
            "t.rejectionCount, t.comment, t.contactEmail, t.isDeleted) " +
            "FROM Tesk t " +
            "LEFT JOIN User cu ON t.creatorUserId = cu.id " +
            "LEFT JOIN User au ON t.assignmentUserId = au.id " +
            "WHERE t.isDeleted = false AND t.assignmentUserId = :userId")
    List<TeskWithNamesDto> findByAssignmentUserIdWithNames(@Param("userId") Long userId);

    /**
     * 获取指定用户创建的任务，包含用户名
     */
    @Query("SELECT new emptyvessel.worklist.dto.TeskWithNamesDto(" +
            "t.id, t.name, t.description, t.creatorUserId, cu.username, " +
            "t.assignmentUserId, au.username, t.startTime, t.endTime, " +
            "t.createdAt, t.status, t.assignedAt, t.submittedAt, t.completedAt, " +
            "t.rejectionCount, t.comment, t.contactEmail, t.isDeleted) " +
            "FROM Tesk t " +
            "LEFT JOIN User cu ON t.creatorUserId = cu.id " +
            "LEFT JOIN User au ON t.assignmentUserId = au.id " +
            "WHERE t.isDeleted = false AND t.creatorUserId = :userId")
    List<TeskWithNamesDto> findByCreatorUserIdWithNames(@Param("userId") Long userId);
}
