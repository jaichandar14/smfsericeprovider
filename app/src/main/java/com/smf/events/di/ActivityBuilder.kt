package com.smf.events.di

import com.smf.events.MainActivity
import com.smf.events.MainModule
import com.smf.events.ui.actionandstatusdashboard.ActionsAndStatusFragment
import com.smf.events.ui.actionandstatusdashboard.ActionsAndStatusModule
import com.smf.events.ui.actiondetails.ActionDetailsFragment
import com.smf.events.ui.actiondetails.ActionDetailsModule
import com.smf.events.ui.bidrejectiondialog.BidRejectionDialogFragment
import com.smf.events.ui.bidrejectiondialog.BidRejectionDialogModule
import com.smf.events.ui.businessregistration.BusinessRegistrationFragment
import com.smf.events.ui.businessregistration.BusinessRegistrationModule
import com.smf.events.ui.commoninformationdialog.CommonInfoDialog
import com.smf.events.ui.commoninformationdialog.CommonInfoDialogModule
import com.smf.events.ui.intiateservicedialog.InitiateServiceDialog
import com.smf.events.ui.dashboard.DashBoardFragment
import com.smf.events.ui.dashboard.DashBoardModule
import com.smf.events.ui.emailotp.EmailOTPFragment
import com.smf.events.ui.emailotp.EmailOTPModule
import com.smf.events.ui.intiateservicedialog.InitiateServiceDialogModule
import com.smf.events.ui.notification.NotificationActivity
import com.smf.events.ui.notification.NotificationModule
import com.smf.events.ui.notification.activenotification.ActiveNotificationFragment
import com.smf.events.ui.notification.activenotification.ActiveNotificationModule
import com.smf.events.ui.notification.oldnotification.OldNotificationFragment
import com.smf.events.ui.notification.oldnotification.OldNotificationModule
import com.smf.events.ui.quotebrief.QuoteBriefFragment
import com.smf.events.ui.quotebrief.QuoteBriefModule
import com.smf.events.ui.quotebriefdialog.QuoteBriefDialog
import com.smf.events.ui.quotebriefdialog.QuoteBriefDialogModule
import com.smf.events.ui.quotedetailsdialog.QuoteDetailsDialog
import com.smf.events.ui.quotedetailsdialog.QuoteDetailsModule
import com.smf.events.ui.schedulemanagement.ScheduleManagementActivity
import com.smf.events.ui.schedulemanagement.ScheduleManagementModule
import com.smf.events.ui.schedulemanagement.calendarfragment.CalendarFragment
import com.smf.events.ui.signin.SignInFragment
import com.smf.events.ui.signin.SignInModule
import com.smf.events.ui.signup.SignUpFragment
import com.smf.events.ui.signup.SignUpModule
import com.smf.events.ui.splash.SplashFragment
import com.smf.events.ui.splash.SplashModule
import com.smf.events.ui.timeslot.TimeSlotsFragment
import com.smf.events.ui.timeslot.deselectingdialog.DeselectingDialogFragment
import com.smf.events.ui.timeslot.deselectingdialog.DeselectingDialogModule
import com.smf.events.ui.timeslotmodifyexpanablelist.DayModifyExpandableListFragment
import com.smf.events.ui.timeslotmodifyexpanablelist.MonthModifyExpandableListFragment
import com.smf.events.ui.timeslotmodifyexpanablelist.WeekModifyExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.DayExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.MonthExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.TimeSlotsExpandableListModule
import com.smf.events.ui.timeslotsexpandablelist.WeekExpandableListFragment
import com.smf.events.ui.vieworderdetails.ViewOrderDetailsDialogFragment
import com.smf.events.ui.vieworderdetails.ViewOrderDetailsModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun provideMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [SplashModule::class])
    abstract fun provideSplashFragment(): SplashFragment

    @ContributesAndroidInjector(modules = [SignInModule::class])
    abstract fun provideSignInFragment(): SignInFragment

    @ContributesAndroidInjector(modules = [SignUpModule::class])
    abstract fun provideSignUpFragment(): SignUpFragment

    @ContributesAndroidInjector(modules = [EmailOTPModule::class])
    abstract fun provideEmailOTPFragment(): EmailOTPFragment

    @ContributesAndroidInjector(modules = [DashBoardModule::class])
    abstract fun provideDashBoardFragment(): DashBoardFragment

    @ContributesAndroidInjector(modules = [BusinessRegistrationModule::class])
    abstract fun provideBusinessRegistrationFragment(): BusinessRegistrationFragment

    @ContributesAndroidInjector(modules = [ActionsAndStatusModule::class])
    abstract fun provideActionsAndStatusFragment(): ActionsAndStatusFragment

    @ContributesAndroidInjector(modules = [ActionDetailsModule::class])
    abstract fun provideActionDetailsFragment(): ActionDetailsFragment

    @ContributesAndroidInjector(modules = [QuoteBriefModule::class])
    abstract fun provideQuoteBriefFragment(): QuoteBriefFragment

    @ContributesAndroidInjector(modules = [QuoteDetailsModule::class])
    abstract fun provideQuoteDetailsDialog(): QuoteDetailsDialog

    @ContributesAndroidInjector(modules = [BidRejectionDialogModule::class])
    abstract fun provideBidRejectionDialog(): BidRejectionDialogFragment

    @ContributesAndroidInjector(modules = [QuoteBriefDialogModule::class])
    abstract fun provideQuoteBriefDialog(): QuoteBriefDialog

    // 2401 - Common Information Dialog Injection
    @ContributesAndroidInjector(modules = [CommonInfoDialogModule::class])
    abstract fun provideCommonInfoDialog(): CommonInfoDialog

    @ContributesAndroidInjector(modules = [InitiateServiceDialogModule::class])
    abstract fun provideInitiateServiceDialog(): InitiateServiceDialog

    // 2402 - View Order Details Injection
    @ContributesAndroidInjector(modules = [ViewOrderDetailsModule::class])
    abstract fun provideViewOrderDetailsDialog(): ViewOrderDetailsDialogFragment

    // 2458
    @ContributesAndroidInjector(modules = [ScheduleManagementModule::class])
    abstract fun provideScheduleManagement(): ScheduleManagementActivity

    // 2458
    @ContributesAndroidInjector(modules = [ScheduleManagementModule::class])
    abstract fun provideCalendarScheduleManagement(): CalendarFragment

    // 2527 - Time Slots ExpandableList Fragment Injection
    @ContributesAndroidInjector(modules = [TimeSlotsExpandableListModule::class])
    abstract fun provideTimeSlotsExpandableListFragment(): DayExpandableListFragment

    // 2670 - Time Slots Week ExpandableList Fragment Injection
    @ContributesAndroidInjector(modules = [TimeSlotsExpandableListModule::class])
    abstract fun provideWeekExpandableListFragment(): WeekExpandableListFragment

    // 2670 - Time Slots Month ExpandableList Fragment Injection
    @ContributesAndroidInjector(modules = [TimeSlotsExpandableListModule::class])
    abstract fun provideMonthExpandableListFragment(): MonthExpandableListFragment

    // 2798 - Time Slots Modify Day ExpandableList Fragment Injection
    @ContributesAndroidInjector(modules = [TimeSlotsExpandableListModule::class])
    abstract fun provideDayModifyExpandableListFragment(): DayModifyExpandableListFragment

    // 2798 - Time Slots Modify Week ExpandableList Fragment Injection
    @ContributesAndroidInjector(modules = [TimeSlotsExpandableListModule::class])
    abstract fun provideWeekModifyExpandableListFragment(): WeekModifyExpandableListFragment

    // 2798 - Time Slots Modify Month ExpandableList Fragment Injection
    @ContributesAndroidInjector(modules = [TimeSlotsExpandableListModule::class])
    abstract fun provideMonthModifyExpandableListFragment(): MonthModifyExpandableListFragment

    // 2803 - Deselection dialog Fragment Injection
    @ContributesAndroidInjector(modules = [DeselectingDialogModule::class])
    abstract fun provideDeselectedDialogFragment(): DeselectingDialogFragment

    // 3103
    @ContributesAndroidInjector(modules = [NotificationModule::class])
    abstract fun provideNotificationActivity(): NotificationActivity

    // 3103
    @ContributesAndroidInjector(modules = [ActiveNotificationModule::class])
    abstract fun provideActiveNotificationFragment(): ActiveNotificationFragment

    // 3103
    @ContributesAndroidInjector(modules = [OldNotificationModule::class])
    abstract fun provideOldNotificationFragment(): OldNotificationFragment

    // 3204 - TimeSlots Fragment Injection
    @ContributesAndroidInjector
    abstract fun provideTimeSlotsFragment(): TimeSlotsFragment

}