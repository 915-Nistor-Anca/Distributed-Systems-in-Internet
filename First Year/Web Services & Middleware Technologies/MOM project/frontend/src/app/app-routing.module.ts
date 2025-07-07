import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomePageComponent } from './features/home-page/home-page.component';
import { AllRecipesPageComponent } from './features/all-recipes-page/all-recipes-page.component';

const routes: Routes = [
  {
    path: '',
    component: HomePageComponent,
  },
  { path: 'home', component: HomePageComponent },
  { path: 'recipes', component: AllRecipesPageComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
