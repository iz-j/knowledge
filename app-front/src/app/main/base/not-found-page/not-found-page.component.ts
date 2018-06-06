import { Component, OnInit, ElementRef, Renderer } from '@angular/core';

import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'not-found-page',
  templateUrl: './not-found-page.component.html',
  styleUrls: ['./not-found-page.component.less']
})
export class NotFoundPageComponent implements OnInit {

  constructor(
    private elementRef: ElementRef,
    private renderer  : Renderer,
    private route: ActivatedRoute,
  ) { }

  ngOnInit() {
    let hoge = 360 * Math.random();
    let color = `hsl(${hoge}, 50%, 50%)`;

    this.renderer.setElementStyle(this.elementRef.nativeElement.querySelector('.center img'), 'background-color', color);
    this.renderer.setElementStyle(this.elementRef.nativeElement.querySelector('.corner img'), 'background-color', color);
    this.renderer.setElementStyle(this.elementRef.nativeElement.querySelector('p'), 'color', color);
  }

}
