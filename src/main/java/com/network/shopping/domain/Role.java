package com.network.shopping.domain;

import com.network.shopping.common.enums.RoleEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "t_role")
@Data
@NoArgsConstructor
public class Role implements Serializable {
    private static final long serialVersionUID = -4077824765334013427L;
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleEnum name;
}
