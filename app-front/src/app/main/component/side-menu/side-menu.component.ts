import { Component, Input, Output, EventEmitter, OnInit, ViewChild } from '@angular/core';
import { AuthService, ClaimSet } from '../../../../app/auth/auth.service';

import { TutorialComponent } from '../tutorial/tutorial.component';

export interface SideMenuItem {
  id?: string;
  label?: string;
  count?: number;
  divider?: boolean;
  active?: boolean;
  search?: boolean;
}

@Component({
  selector: 'side-menu',
  templateUrl: './side-menu.component.html',
  styleUrls: ['./side-menu.component.less']
})
export class SideMenuComponent implements OnInit {
  @Input() menus: SideMenuItem[];
  @Output() change = new EventEmitter<SideMenuItem>();

  @ViewChild('tutorial') tutorial: TutorialComponent;

  user: ClaimSet;

  constructor(
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.user = this.auth.claim;
  }

  onClick(menu: SideMenuItem) {
    if (menu.divider || menu.active) {
      return;
    }
    this.menus.forEach(m => m.active = m.id === menu.id);
    this.change.emit(menu);
  }

  onHelpClick() {
    this.tutorial.show();
  }
}
