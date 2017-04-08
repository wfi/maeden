package org.maeden.controller;

import org.maeden.simulator.GOBAgent;
import org.maeden.simulator.Grid;

interface Senses {

    public void getInfo(GOBAgent a, Grid g);

    public void sendInfo(GOBAgent a, XMLobject? senseInfo);
    
}
	
