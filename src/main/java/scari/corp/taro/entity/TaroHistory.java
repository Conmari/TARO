package scari.corp.taro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import scari.corp.taro.enums.LayoutType;

import java.time.LocalDateTime;

@Entity
@Table(name = "taro_history", indexes = {
        @Index(name = "idx_history_user_date", columnList = "user_id, created_at DESC"),
        @Index(name = "idx_history_session_date", columnList = "session_id, created_at DESC")
})
@Data
@Builder
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "session_id")
    private String sessionId;
}
