import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

import { Edge } from '../../shared/edge/edge';
import { DefaultTypes } from '../../shared/service/defaulttypes';
import { Utils, Websocket } from '../../shared/shared';
import { ConfigImpl } from '../../shared/edge/config';
import { CurrentDataAndSummary } from '../../shared/edge/currentdata';
import { Widget } from '../../shared/type/widget';
import { CustomFieldDefinition } from '../../shared/type/customfielddefinition';
import { environment } from '../../../environments';

@Component({
  selector: 'overview',
  templateUrl: './overview.component.html'
})
export class OverviewComponent implements OnInit, OnDestroy {

  public edge: Edge = null
  public config: ConfigImpl = null;
  public currentData: CurrentDataAndSummary = null;
  public widgets: Widget[] = [];
  //public customFields: CustomFieldDefinition = {};

  private stopOnDestroy: Subject<void> = new Subject<void>();
  private currentDataTimeout: number;

  constructor(
    public websocket: Websocket,
    private route: ActivatedRoute,
    public utils: Utils
  ) { }

  ngOnInit() {
    this.websocket.setCurrentEdge(this.route)
      .takeUntil(this.stopOnDestroy)
      .subscribe(edge => {
        this.edge = edge;
        if (edge == null) {
          this.config = null;
        } else {

          edge.config
            .takeUntil(this.stopOnDestroy)
            .subscribe(config => {
              this.config = config;
              if (config != null) {
                // get widgets
                this.widgets = config.getWidgets();

                this.subscribe();
              }
              // TODO fieldstatus
              // /*
              //  * Add custom fields for fieldstatus component
              //  */
              // for (let thing in this.customFields) {
              //   let thingChannels = []
              //   for (let channel in this.customFields[thing]) {
              //     thingChannels.push(channel);
              //   }
              //   channels[thing] = thingChannels;
              // }
            });
        }
      });
  }

  ngOnDestroy() {
    clearInterval(this.currentDataTimeout);
    if (this.edge) {
      this.edge.unsubscribeCurrentData();
    }
    this.edge = null;
    this.config = null;
    this.currentData = null;
    this.stopOnDestroy.next();
    this.stopOnDestroy.complete();
  }

  private requiredSubscribes: { [componentId: string]: DefaultTypes.ChannelAddresses } = {};

  onRequiredSubscribes(componentId: string, subscribes: DefaultTypes.ChannelAddresses) {
    this.requiredSubscribes[componentId] = subscribes;
    this.subscribe();
  }

  private subscription: Subscription = null;

  private subscribe() {
    // abort if edge or config are missing
    if (this.edge == null || this.config == null) {
      if (this.subscription != null) {
        this.subscription.unsubscribe();
      }
      return;
    }
    // merge channels from requiredSubscribes
    //let channels: DefaultTypes.ChannelAddresses = {} // TODO move getImportantChannels to sub-components + event
    let channels: DefaultTypes.ChannelAddresses = this.config.getImportantChannels();
    for (let componentId in this.requiredSubscribes) {
      let requiredSubscribe = this.requiredSubscribes[componentId];
      for (let thingId in requiredSubscribe) {
        if (thingId in channels) {
          for (let channelId of requiredSubscribe[thingId]) {
            if (!channels[thingId].includes(channelId)) {
              channels[thingId].push(channelId);
            }
          }
        } else {
          channels[thingId] = requiredSubscribe[thingId];
        }
      }
    }

    if (this.subscription != null) {
      this.subscription.unsubscribe();
    }
    this.subscription = this.edge.subscribeCurrentData(channels)
      .takeUntil(this.stopOnDestroy)
      .subscribe(currentData => {
        this.currentData = currentData;

        // resubscribe on timeout
        clearInterval(this.currentDataTimeout);
        this.currentDataTimeout = window.setInterval(() => {
          this.currentData = null;
          if (this.websocket.status == 'online') {
            // TODO
            console.warn('timeout...')
          }
        }, Websocket.TIMEOUT);
      });
  }
}