package nl.tudelft.healthblocks.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResearcherDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String affiliation;
}
