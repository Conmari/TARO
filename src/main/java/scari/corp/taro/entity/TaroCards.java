package scari.corp.taro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import scari.corp.taro.embeddable.Meanings;
import scari.corp.taro.enums.Arcana;

@Entity
@Table(name = "taro_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaroCards {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nameEn;

    @Column(nullable = false, unique = true)
    private String nameRu;

    @Enumerated(EnumType.STRING)
    private Arcana arcana;

    private String suit;

    private Integer number;

    @Embedded
    private Meanings meanings;

    @Column(name = "image_key", nullable = false)
    private String imageKey;
}
