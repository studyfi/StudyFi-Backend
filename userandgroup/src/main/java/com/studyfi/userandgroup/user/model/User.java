package com.studyfi.userandgroup.user.model;

import com.studyfi.userandgroup.group.model.Group;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;  // Changed from Long to Integer

    private String name;

    private String email;

    private String password;

    private String phoneContact;

    private String birthDate;

    private String country;

    private String aboutMe;

    private String currentAddress;

    @Column(name = "profile_image_url")
    private String profileImageUrl;



    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "verification_code")
    private String verificationCode;

    // for storing the expiration time of the verification code
    @Column(name = "verification_code_expiry")
    private Date verificationCodeExpiry;  // The expiration time of the verification token

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_group",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    private List<Group> groups;  // Many-to-many relationship with Group



    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    public String getCoverImageUrl() {
        return coverImageUrl;
    }
    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }
}