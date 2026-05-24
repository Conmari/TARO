package scari.corp.taro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private TaroCards card;

    @Column(name = "is_reversed", nullable = false, columnDefinition = "boolean default false")
    private boolean isReversed;

    @Column(name = "card_order", nullable = false, columnDefinition = "integer default 0")
    private int cardOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layout_id", nullable = false)
    private TaroLayout layout;
}
