package derpulo.maps.planet;

import mindustry.maps.planet.SerpuloPlanetGenerator;
import mindustry.type.*;

public class DerpuloPlanetGenerator extends SerpuloPlanetGenerator{
    @Override
    public void generateSector(Sector sector){
        sector.generateEnemyBase = true;
    }
}