package pers.dog.app.service.impl;

import java.io.File;
import java.util.List;

import javafx.scene.control.TreeItem;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import pers.dog.app.service.ProjectService;
import pers.dog.app.service.RecycleService;
import pers.dog.domain.entity.Project;
import pers.dog.domain.entity.Recycle;
import pers.dog.domain.repository.RecycleRepository;
import pers.dog.infra.constant.ProjectType;

/**
 * @author 废柴 2023/9/25 15:42
 */
@Service
public class RecycleServiceImpl implements RecycleService {
    private final RecycleRepository recycleRepository;
    private final ProjectService projectService;

    public RecycleServiceImpl(RecycleRepository recycleRepository, ProjectService projectService) {
        this.recycleRepository = recycleRepository;
        this.projectService = projectService;
    }

    @Override
    public List<Recycle> list(String keyword) {
        if (ObjectUtils.isEmpty(keyword)) {
            return recycleRepository.findAll();
        }
        keyword = "%" + keyword + "%";
        return recycleRepository.findByProjectNameLikeIgnoreCaseOrLocationLikeIgnoreCase(keyword, keyword);
    }

    @Override
    public Project recover(Recycle recycle) {
        TreeItem<Project> project = projectService.createFile(ProjectType.FILE, recycle.getFileType(), recycle.getProjectName(), recycle.getContent(),
                ObjectUtils.isEmpty(recycle.getLocation()) ? null : StringUtils.split(recycle.getLocation(), File.separator));
        if (project != null) {
            recycleRepository.deleteById(recycle.getRecycleId());
            return project.getValue();
        }
        return null;
    }

    @Override
    public void delete(Recycle recycle) {
        recycleRepository.deleteById(recycle.getRecycleId());
    }
}
