package com.tank.companion;

public class TankCoreTest {
    public static void main(String[] args) throws Exception {
        TankCore.Result r = TankCore.calculate("TANKS", "BOOTSTRAP");
        System.out.println(r.coordinate);
        System.out.println(r.kalebration);
        System.out.println(r.cicada);
        System.out.println(r.distorted);
        System.out.println(r.waypoint);
        System.out.println(r.password);
    }
}
