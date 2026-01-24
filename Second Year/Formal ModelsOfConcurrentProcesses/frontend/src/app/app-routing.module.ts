import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomePageComponent } from './features/home-page/home-page.component';
import { AllAnimalsPageComponent } from './features/all-animals-page/all-animals-page.component';
import { OwnersPageComponent } from './features/owners-page/owners-page.component';

const routes: Routes = [
  {
    path: '',
    component: HomePageComponent,
  },
  { path: 'home', component: HomePageComponent },
  { path: 'animals', component: AllAnimalsPageComponent },
  {path: 'owners', component: OwnersPageComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
