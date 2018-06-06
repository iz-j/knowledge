import { Injectable } from '@angular/core';
import { CanDeactivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

/**
 * When you need to do something BeforeUnload, need to:
 * - implement OnBeforeunload to your component
 * - implement onBeforeUnload(event) with @HostListener('window:beforeunload', ['$event']) as sample.
 * - add "canDeactivate: [BeforeunloadGuard]" to your component page's routing of routing.module.ts
 *   ex) { path: 'quote-reply/input/:quotationId', component: QuoteReplyInputPageComponent, data: { title: 'quotation_making' }, canDeactivate: [BeforeunloadGuard] },
 */
export abstract class OnBeforeunload {
  onBeforeUnload: (event: any) => boolean;

  /**
   * onBeforeunload sample
   */
  // @HostListener('window:beforeunload', ['$event'])
  // onBeforeUnload(event: any) {
  //   doSomething;
  //   return true; // false if blocking
  // }
}

@Injectable()
export class BeforeunloadGuard implements CanDeactivate<OnBeforeunload> {
  canDeactivate(component: OnBeforeunload, currentRoute: ActivatedRouteSnapshot, currentState: RouterStateSnapshot, nextState?: RouterStateSnapshot) {
    if (!component.onBeforeUnload) return true;
    return component.onBeforeUnload({
      currentRoute: currentRoute,
      currentState: currentState,
      nextState: nextState,
    });
  }
}