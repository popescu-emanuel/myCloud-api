package fmi.unibuc.ro.mycloudapi.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "type"
        , uniqueConstraints = {@UniqueConstraint(columnNames = "type")}
)
@NoArgsConstructor
public class SizePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SubscriptionType type;

    @Column(name = "capacity")
    private Integer capacity;

    public SizePlan(SubscriptionType type, Integer capacity) {
        this.type = type;
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
