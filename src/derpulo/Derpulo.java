package derpulo;

import arc.*;
import arc.math.Mathf;
import arc.util.*;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.mod.*;
import derpulo.content.*;
import mindustry.type.Sector;

import static mindustry.Vars.*;

public class Derpulo extends Mod{

// идеи:
//    1. спавн юнитов с взрывоопасным ресурсами

    public final String tag = "DS";
    public String name;
    private int steps =0;

    public Derpulo() {}

    void DSLogInfo(String text) {
        Log.info("[" + tag +"] " + text);
    }

    private void dSRunTurn() {
        steps++;

        if(state.isCampaign() && state.getPlanet() == DerpuloPlanet.derpulo) {
            for (Sector sector : state.getPlanet().sectors) {

                //Instantly invasion on a captured sectors
                if (sector.hasBase()
                    && !sector.isAttacked()
                    && sector.info.hasSpawns
                    && sector.preset == null
                ) {
                    sectorInvasion(sector);
                }

                //Rare invasions or lost
                if(steps > 60 && sector.preset != null && sector.hasBase() && !sector.isBeingPlayed() && Math.random() > 0.999999) {
                    steps = 0;

                    sectorInvasion(sector);
//                    sector.info.attack = true;
//                    Events.fire(new EventType.SectorInvasionEvent(sector));
//
//                    Events.fire(new EventType.SectorLoseEvent(sector));
//
//                    sector.info.items.clear();
//                    sector.info.damage = 1f;
//                    sector.info.hasCore = false;
//                    sector.info.production.clear();

                }

                sector.saveInfo();
            }
        }
    }


    @Override
    public void init() {
        if (state.isCampaign()) {
            //Cleared sector after lose
            Events.on(EventType.SectorLoseEvent.class, e -> {
                if (e.sector.planet == DerpuloPlanet.derpulo && e.sector.preset == null) {
                    name = e.sector.info.name;
                    e.sector.save.delete();
                    DSLogInfo("sector " + name + " has been cleared by SectorLoseEvent");
                }
            });
            Events.on(EventType.GameOverEvent.class, e -> {
                GameState s = state;
                name = state.getSector().name();
                if (s.isCampaign() && s.getPlanet() == DerpuloPlanet.derpulo && s.getSector().preset == null) {
                    state.getSector().save.delete();
                    state.rules.sector.save = null;
                    state.rules.sector.info.attack = true;
                    state.rules.sector.info.items.clear();
                    state.rules.sector.info.damage = 1f;
                    state.rules.sector.info.hasCore = false;
                    state.rules.sector.info.production.clear();

                    DSLogInfo("sector " + name + " has been cleared by GameOverEvent");
                }
            });

            Events.run(EventType.Trigger.update, this::dSRunTurn);
        }
    }


    @Override
    public void loadContent(){
        DerpuloPlanet.load();
        DerpuloSectorPresets.load();
        DerpuloTechTree.load();
    }

    public void sectorInvasion(Sector sector) {
        int waveMax = Math.max(sector.info.winWave, sector.isBeingPlayed() ?
                state.wave : sector.info.wave + sector.info.wavesPassed) + Mathf.random(2, 4) * 5;

        DSLogInfo("" + waveMax);
        if (sector.isBeingPlayed()) {
            state.rules.winWave = waveMax;
            state.rules.waves = true;
            state.rules.attackMode = false;

            if (net.server())
                Call.setRules(state.rules);
        } else {
            sector.info.winWave = waveMax;
            sector.info.waves = true;
            sector.info.attack = false;
            sector.saveInfo();
        }

        Events.fire(new EventType.SectorInvasionEvent(sector));
    }

}
