package scari.corp.taro.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import scari.corp.taro.enums.Arcana;

@Entity
@Table(name = "taro_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaroCards {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private Arcana arcana;

    private String suit;

    private Integer number;

    @Column(length = 2000)
    private String uprightMeaning;

    @Column(length = 2000)
    private String reversedMeaning;
}
