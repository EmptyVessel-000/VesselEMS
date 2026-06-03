package emptyvessel.worklist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import emptyvessel.worklist.model.Config;

@Repository
public interface ConfigRepository extends JpaRepository<Config, Long> {

    Optional<Config> findByConfigKey(String configKey);

    boolean existsByConfigKey(String configKey);
}