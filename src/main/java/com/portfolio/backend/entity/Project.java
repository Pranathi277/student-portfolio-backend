package com.portfolio.backend.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String image;

    private String github;

    @ElementCollection
    @CollectionTable(name = "project_tech", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "tech")
    private List<String> tech;

    private String date;

    private Integer views = 0;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    private String author;

    private String authorEmail;

    public Project() {
    }

    public Project(Long id, String title, String description, String image, String github, List<String> tech, String date, Integer views, Boolean isPublic, String author, String authorEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.image = image;
        this.github = github;
        this.tech = tech;
        this.date = date;
        this.views = views;
        this.isPublic = isPublic;
        this.author = author;
        this.authorEmail = authorEmail;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }
    
    public List<String> getTech() { return tech; }
    public void setTech(List<String> tech) { this.tech = tech; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }
    
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
}
