package scari.corp.taro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import scari.corp.taro.enums.LayoutType;

import java.time.LocalDateTime;

@Entity
@Table(name = "taro_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaroHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LayoutType layoutType;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private TaroCards card;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
