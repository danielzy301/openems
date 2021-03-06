import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';
import { Subject } from 'rxjs/Subject';
import { format } from 'date-fns';

import { Edge } from '../../../shared/edge/edge';
import { Websocket } from '../../../shared/shared';
import { DefaultTypes } from '../../../shared/service/defaulttypes';

@Component({
  selector: 'log',
  templateUrl: './log.component.html'
})
export class LogComponent implements OnInit, OnDestroy {

  public edge: Edge = null;
  public logs: DefaultTypes.Log[] = [];
  public isSubscribed: boolean = false;

  private MAX_LOG_ENTRIES = 200;
  private stopOnDestroy: Subject<void> = new Subject<void>();
  private messageId = null;

  constructor(
    private route: ActivatedRoute,
    private websocket: Websocket,
  ) { }

  ngOnInit() {
    this.websocket.setCurrentEdge(this.route)
      .takeUntil(this.stopOnDestroy)
      .subscribe(edge => {
        this.edge = edge;
        this.subscribeLog();
      });
  }

  public toggleSubscribe($event: any /*MdSlideToggleChange*/) {
    if ($event.checked) {
      this.subscribeLog();
    } else {
      this.unsubscribeLog();
    }
  }

  public subscribeLog() {
    // put placeholder
    if (this.logs.length > 0) {
      this.logs.unshift({
        time: "-------------------",
        level: "----",
        color: "black",
        message: "",
        source: ""
      });
    }

    if (this.edge != null) {
      const result = this.edge.subscribeLog();
      this.messageId = result.messageId;
      result.logs.takeUntil(this.stopOnDestroy).subscribe(log => {
        log.time = format(new Date(<number>log.time * 1000), "DD.MM.YYYY HH:mm:ss");
        switch (log.level) {
          case 'INFO':
            log.color = 'green';
            break;
          case 'WARN':
            log.color = 'orange';
            break;
          case 'DEBUG':
            log.color = 'gray';
            break;
          case 'ERROR':
            log.color = 'red';
            break;
        };
        this.logs.unshift(log);
        if (this.logs.length > this.MAX_LOG_ENTRIES) {
          this.logs.length = this.MAX_LOG_ENTRIES;
        }
      });
      this.isSubscribed = true;
    };
  }

  public unsubscribeLog() {
    if (this.edge != null) {
      this.edge.unsubscribeLog(this.messageId);
    }
    this.stopOnDestroy.next();
    this.stopOnDestroy.complete();
    this.isSubscribed = false;
  };

  ngOnDestroy() {
    this.unsubscribeLog();
  }
}