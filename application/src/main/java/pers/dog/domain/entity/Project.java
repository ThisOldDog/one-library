package pers.dog.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import pers.dog.infra.constant.FileType;
import pers.dog.infra.constant.ProjectType;

/**
 * @author 废柴 2023/2/21 20:30
 */
@Entity
@Table(name = "ol_project")
public class Project {
    public static final String FIELD_SORT_INDEX = "sortIndex";
    @Id
    @Column(length = 60)
    @GeneratedValue
    private Long projectId;
    @Column(nullable = false, length = 120)
    private String projectName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectType projectType;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FileType fileType;
    @Column(length = 60)
    private Long parentProjectId;

    @Column(nullable = false)
    private Integer sortIndex;

    /* Transient */
    @Transient
    private String simpleProjectName;
    @Transient
    private String newProjectName;
    @Transient
    private Project parent;

    /* Function */
    public String getSimpleProjectName() {
        if (ProjectType.DIRECTORY.equals(projectType)) {
            return projectName;
        }
        if (simpleProjectName == null && projectName != null) {
            int suffixIndex = projectName.lastIndexOf(".");
            if (suffixIndex == -1) {
                simpleProjectName = projectName;
            } else {
                simpleProjectName = projectName.substring(0, suffixIndex);
            }
        }
        return simpleProjectName;
    }

    public Project rename(String text) {
        if (ProjectType.DIRECTORY.equals(projectType)) {
            return setNewProjectName(text);
        }
        return setNewProjectName(text + "." + fileType.getSuffix());
    }

    /* Getter / Setter */

    public Long getProjectId() {
        return projectId;
    }

    public Project setProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getProjectName() {
        return projectName;
    }

    public Project setProjectName(String projectName) {
        this.projectName = projectName;
        this.simpleProjectName = null;
        return this;
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public Project setProjectType(ProjectType projectType) {
        this.projectType = projectType;
        return this;
    }

    public FileType getFileType() {
        return fileType;
    }

    public Project setFileType(FileType fileType) {
        this.fileType = fileType;
        return this;
    }

    public Long getParentProjectId() {
        return parentProjectId;
    }

    public Project setParentProjectId(Long parentProjectId) {
        this.parentProjectId = parentProjectId;
        return this;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }

    public Project setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
        return this;
    }

    public String getNewProjectName() {
        return newProjectName;
    }

    public Project setNewProjectName(String newProjectName) {
        this.newProjectName = newProjectName;
        return this;
    }

    public Project getParent() {
        return parent;
    }

    public Project setParent(Project parent) {
        this.parent = parent;
        return this;
    }
}
