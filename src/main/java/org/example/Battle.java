package org.example;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Battle {
    @JsonAlias({"Id"})
    private UUID id;
    @JsonAlias({"Username1"})
    private String user1Username;
    @JsonAlias({"Username2"})
    private String user2Username;
}
