import {
  trigger,
  transition,
  style,
  query,
  group,
  animate,
  AnimationTriggerMetadata
} from '@angular/animations';

export const routerTransition: AnimationTriggerMetadata = trigger('routerTransition', [
  transition('* <=> *', [
    query(':enter, :leave', [
      style({
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        minHeight: '100%'
      })
    ], { optional: true }),

    query(':enter', [
      style({ opacity: 0, transform: 'translateX(30px)' })
    ], { optional: true }),

    group([
      query(':leave', [
        animate('200ms cubic-bezier(0.4, 0, 0.2, 1)',
          style({ opacity: 0, transform: 'translateX(-30px)' })
        )
      ], { optional: true }),

      query(':enter', [
        animate('300ms 100ms cubic-bezier(0.16, 1, 0.3, 1)',
          style({ opacity: 1, transform: 'translateX(0)' })
        )
      ], { optional: true })
    ])
  ])
]);
