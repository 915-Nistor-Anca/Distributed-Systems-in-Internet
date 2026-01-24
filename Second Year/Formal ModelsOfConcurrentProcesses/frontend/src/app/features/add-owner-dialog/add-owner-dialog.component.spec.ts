import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddOwnerDialogComponent } from './add-owner-dialog.component';

describe('AddOwnerDialogComponent', () => {
  let component: AddOwnerDialogComponent;
  let fixture: ComponentFixture<AddOwnerDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AddOwnerDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddOwnerDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
