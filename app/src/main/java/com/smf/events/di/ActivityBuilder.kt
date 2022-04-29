package com.example.demodragger.di

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
import com.smf.events.ui.dashboard.DashBoardFragment
import com.smf.events.ui.dashboard.DashBoardModule
import com.smf.events.ui.emailotp.EmailOTPFragment
import com.smf.events.ui.emailotp.EmailOTPModule
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
import com.smf.events.ui.timeslotsexpandablelist.TimeSlotsExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.TimeSlotsExpandableListModule
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

    // 2402 - View Order Details Injection
    @ContributesAndroidInjector(modules = [ViewOrderDetailsModule::class])
    abstract fun provideViewOrderDetailsDialog(): ViewOrderDetailsDialogFragment

    // 2458
    @ContributesAndroidInjector(modules = [ScheduleManagementModule::class])
    abstract fun provideScheduleManagement(): ScheduleManagementActivity

    // 2458
    @ContributesAndroidInjector(modules = [ScheduleManagementModule::class])
    abstract fun provideCalendarScheduleManagement(): CalendarFragment

    // 2527 - Time Slots Fragment Injection
    @ContributesAndroidInjector(modules = [ScheduleManagementModule::class])
    abstract fun provideTimeSlotsFragment(): TimeSlotsFragment

    // 2527 - Time Slots ExpandableList Fragment Injection
    @ContributesAndroidInjector(modules = [TimeSlotsExpandableListModule::class])
    abstract fun provideTimeSlotsExpandableListFragment(): TimeSlotsExpandableListFragment
}