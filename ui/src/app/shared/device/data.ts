import { Config } from './config';

export class Summary {
    public readonly storage = {
        soc: null,
        activePower: 0,
        maxActivePower: 0
    };
    public readonly production = {
        powerRatio: 0,
        activePower: 0, // sum of activePowerAC and activePowerDC
        activePowerAC: 0,
        activePowerDC: 0,
        maxActivePower: 0
    };
    public readonly grid = {
        powerRatio: 0,
        activePower: 0,
        maxActivePower: 0
    };
    public readonly consumption = {
        powerRatio: 0,
        activePower: 0
    };

    /**
     * Calculate summary data from websocket reply
     */
    constructor(config: Config, data: ChannelData) {
        function getActivePower(o: any): number {
            if ("ActivePowerL1" in o && o.ActivePowerL1 != null && "ActivePowerL2" in o && o.ActivePowerL2 != null && "ActivePowerL3" in o && o.ActivePowerL3 != null) {
                return o.ActivePowerL1 + o.ActivePowerL2 + o.ActivePowerL3;
            } else if ("ActivePower" in o && o.ActivePower != null) {
                return o.ActivePower;
            } else {
                return 0;
            }
        }

        {
            /*
             * Storage
             */
            let soc = 0;
            let activePower = 0;
            let essThings = config.storageThings;
            let countSoc = 0;
            for (let thing of essThings) {
                if (thing in data) {
                    let ess = data[thing];
                    if ("Soc" in ess && ess.Soc != null) {
                        soc += ess.Soc;
                        countSoc += 1;
                    }
                    activePower += getActivePower(ess);
                }
            }
            this.storage.soc = soc / countSoc;
            this.storage.activePower = activePower;
        }

        {
            /*
             * Grid
             */
            let powerRatio = 0;
            let activePower = 0;
            let maxActivePower = 0;
            for (let thing of config.gridMeters) {
                if (thing in data) {
                    let thingChannels = config._meta.natures[thing].channels;
                    let meter = data[thing];
                    let power = getActivePower(meter);
                    if (thingChannels["maxActivePower"]) {
                        if (activePower > 0) {
                            powerRatio = (power * 50.) / thingChannels["maxActivePower"]["value"]
                        } else {
                            powerRatio = (power * -50.) / thingChannels["minActivePower"]["value"]
                        }
                    } else {
                        console.log("no maxActivePower Grid");
                    }
                    activePower += power;
                    maxActivePower += thingChannels["maxActivePower"]["value"];
                }
            }
            this.grid.powerRatio = powerRatio;
            this.grid.activePower = activePower;
            this.grid.maxActivePower = maxActivePower;
        }

        {
            /*
             * Production
             */
            let powerRatio = 0;
            let activePowerAC = 0;
            let activePowerDC = 0;
            let maxActivePower = 0;
            for (let thing of config.productionMeters) {
                if (thing in data) {
                    let thingChannels = config._meta.natures[thing].channels;
                    let meter = data[thing];
                    activePowerAC += getActivePower(meter);
                    if ("ActualPower" in meter && meter.ActualPower != null) {
                        activePowerDC += meter.ActualPower;
                    }
                    if (thingChannels["maxActivePower"]) {
                        maxActivePower += thingChannels["maxActivePower"]["value"];
                    } else {
                        // no maxActivePower
                    }
                    if (thingChannels["maxActualPower"]) {
                        maxActivePower += thingChannels["maxActualPower"]["value"];
                    } else {
                        // no maxActualPower
                    }
                }
            }

            // correct negative production
            if (activePowerAC < 0) {
                // console.warn("negative production? ", this)
                activePowerAC = 0;
            }
            if (maxActivePower < 0) { maxActivePower = 0; }

            if (maxActivePower == 0) {
                powerRatio = 100;
            } else {
                powerRatio = ((activePowerAC + activePowerDC) * 100.) / maxActivePower;
            }

            this.production.powerRatio = powerRatio;
            this.production.activePowerAC = activePowerAC;
            this.production.activePowerDC = activePowerDC;
            this.production.activePower = activePowerAC + activePowerDC;
            this.production.maxActivePower = maxActivePower;
        }

        {
            /*
             * Consumption
             */
            let activePower = this.grid.activePower + this.production.activePowerAC + this.storage.activePower;
            let maxActivePower = this.grid.maxActivePower + this.production.maxActivePower + this.storage.maxActivePower;
            this.consumption.powerRatio = (activePower * 100.) / maxActivePower;
            this.consumption.activePower = activePower;
        }
    }
}

export class ChannelData {
    [thing: string]: {
        [channel: string]: number;
    }
}

export class Data {
    public readonly summary: Summary;

    constructor(
        public readonly data: ChannelData,
        private config: Config
    ) {
        this.summary = new Summary(config, data);
    }
}


//   private getkWhResult(channels: { [thing: string]: [string] }): { [thing: string]: [string] } {
//     let kWh = {};
//     let thingChannel = [];

//     for (let type in this.things) {
//       for (let thing in this.things[type]) {
//         for (let channel in channels[thing]) {
//           if (channels[thing][channel] == "ActivePower") {
//             kWh[thing + "/ActivePower"] = type;
//           } else if (channels[thing][channel] == "ActivePowerL1" || channels[thing][channel] == "ActivePowerL2" || channels[thing][channel] == "ActivePowerL3") {
//             kWh[thing + "/ActivePowerL1"] = type;
//             kWh[thing + "/ActivePowerL2"] = type;
//             kWh[thing + "/ActivePowerL3"] = type;
//           }
//         }
//       }
//     }

//     return kWh;
//   }