package scari.corp.taro.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meanings {

    @Column(length = 2000)
    private String upright;

    @Column(length = 2000)
    private String reversed;
}
