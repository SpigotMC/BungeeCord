package net.md_5.bungee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.Location;

@Getter
@Setter
@AllArgsConstructor
@ToString(includeFieldNames = true)
public class UserLocation implements Location {

    private double x, y, z;
    private float yaw, pitch;
    private int dimension;
    private ServerConnection server;
    
}
