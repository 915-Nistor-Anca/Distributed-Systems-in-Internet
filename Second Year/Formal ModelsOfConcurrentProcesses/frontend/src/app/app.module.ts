import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { Store, StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HeaderComponent } from './features/header/header.component';
import { AllAnimalsPageComponent } from './features/all-animals-page/all-animals-page.component';
import { HomePageComponent } from './features/home-page/home-page.component';
import { ANIMAL_REDUCER } from './store/reducers/animal.reducers';
import { AnimalEffects } from './store/effects/animal.effects';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToastrModule } from 'ngx-toastr';
import { AddAnimalDialogComponent } from './features/add-animal-dialog/add-animal-dialog.component';
import { MatDialogModule } from '@angular/material/dialog';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { SPECIES_REDUCER } from './store/reducers/owner.reducers';
import { OWNERS_REDUCER } from './store/reducers/species.effects';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    AllAnimalsPageComponent,
    HomePageComponent,
    AddAnimalDialogComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MatTableModule,
    MatPaginatorModule,
    HttpClientModule,
    FormsModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCheckboxModule,
    BrowserAnimationsModule,
    MatDialogModule,
    ReactiveFormsModule,
    ToastrModule.forRoot(),
    StoreModule.forFeature('animal', ANIMAL_REDUCER),
    StoreModule.forFeature('species', SPECIES_REDUCER),
    StoreModule.forFeature('owners', OWNERS_REDUCER),
    EffectsModule.forFeature([AnimalEffects]),
    StoreModule.forRoot({}, {}),
    EffectsModule.forRoot([]),
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
