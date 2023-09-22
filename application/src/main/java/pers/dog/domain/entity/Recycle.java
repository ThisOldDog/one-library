package pers.dog.domain.entity;

import java.time.ZonedDateTime;
import javax.persistence.*;

import pers.dog.infra.constant.FileType;

/**
 * @author 废柴 2023/9/22 15:03
 */
@Entity
@Table(name = "ol_recycle")
public class Recycle {
    @Id
    @Column(length = 60)
    @GeneratedValue
    private Long recycleId;

    @Column(nullable = false, length = 120)
    private String projectName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FileType fileType;

    @Column(nullable = false, length = 2400)
    private String location;

    @Column(nullable = false)
    private ZonedDateTime deleteDateTime;

    @Column
    private byte[] content;

    public Long getRecycleId() {
        return recycleId;
    }

    public Recycle setRecycleId(Long recycleId) {
        this.recycleId = recycleId;
        return this;
    }

    public String getProjectName() {
        return projectName;
    }

    public Recycle setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public FileType getFileType() {
        return fileType;
    }

    public Recycle setFileType(FileType fileType) {
        this.fileType = fileType;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public Recycle setLocation(String location) {
        this.location = location;
        return this;
    }

    public ZonedDateTime getDeleteDateTime() {
        return deleteDateTime;
    }

    public Recycle setDeleteDateTime(ZonedDateTime deleteDateTime) {
        this.deleteDateTime = deleteDateTime;
        return this;
    }

    public byte[] getContent() {
        return content;
    }

    public Recycle setContent(byte[] content) {
        this.content = content;
        return this;
    }
}
