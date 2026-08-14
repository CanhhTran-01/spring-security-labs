package vn.edu.kma.securityoauth2.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.kma.securityoauth2.enums.RoleCode;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Roles extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private RoleCode code;

    @Column(length = 1000)
    private String description;
}
