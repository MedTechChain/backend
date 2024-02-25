package nl.tudelft.healthblocks.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * A DTO class for a researcher that will be sent when researchers have been requested.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResearcherDTO {

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("affiliation")
    private String affiliation;
}
