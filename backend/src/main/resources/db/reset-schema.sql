TRUNCATE TABLE
    users,
    linked_accounts,
    scheduled_report_jobs,
    one_time_scheduled_report_entity,
    periodic_scheduled_report_entity,
    scheduled_reports,
    report
RESTART IDENTITY CASCADE;