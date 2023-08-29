package pers.dog.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pers.dog.domain.entity.Project;

/**
 * @author 废柴 2023/2/21 23:21
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    @Query("SELECT MAX(sortIndex) FROM Project WHERE parentProjectId IS NULL")
    Optional<Integer> findMaxSortIndex();

    @Query("SELECT MAX(sortIndex) FROM Project WHERE parentProjectId = :parentProjectId")
    Optional<Integer> findMaxSortIndex(long parentProjectId);

    List<Project> findByParentProjectId(Long parentProjectId);
}
