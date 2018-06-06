import { NgModule, ModuleWithProviders } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { hogeLibModule } from './../../../lib/module';

import { PageRootComponent } from './page-root/page-root.component';
import { PaginationComponent } from './pagination/pagination.component';
import { ActivitiesComponent } from './activities/activities.component';
import { ActivityService } from './activities/activity.service';
import { SideMenuComponent } from './side-menu/side-menu.component';
import { DetailLinesComponent } from './detail-lines/detail-lines.component';
import { TutorialComponent } from './tutorial/tutorial.component';
import { PeriodChooserComponent } from './period/period-chooser.component';
import { PeriodService } from './period/period.service';
import { CommentsComponent } from './comment/comments.component';
import { CommentService } from './comment/comment.service';

const COMPONENTS = [
  PageRootComponent,
  PaginationComponent,
  ActivitiesComponent,
  SideMenuComponent,
  DetailLinesComponent,
  TutorialComponent,
  PeriodChooserComponent,
  CommentsComponent,
];

const SERVICES = [
  ActivityService,
  PeriodService,
  CommentService,
];

@NgModule({
  imports: [CommonModule, FormsModule, hogeLibModule],
  exports: COMPONENTS,
  declarations: COMPONENTS,
  providers: SERVICES
})
export class ComponentModule { }
