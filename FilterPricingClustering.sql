SELECT 
       [fleet_is_common_numbered]
      ,[fleet_category_code]
      ,[fleet_category_description]
      ,[fleet_type_code]
      ,[fleet_type_description]
      ,[fleet_current_pricegroup]
      ,[hire_no]
      ,[hire_line_no]
      ,[hire_shift]
      ,[hire_created_by_name]
      ,[hire_line_is_subhire]
      ,[hire_line_subhire_creditor_code]
      ,[hire_line_type_description]
      ,[hire_line_package_name]
      ,[hire_line_flat_monthly_charge]
      ,[hire_line_block_charge_indicator]
      ,[customer_parent_group]
      ,[customer_code]
      ,[customer_name]
      ,[customer_tendered_flag]
      ,[customer_pricing_area]
      ,[invoice_posted_date]
      ,[invoice_type]
      ,[invoice_line_billable_days]
      ,[invoice_line_total_exGST]
      ,hire_line_invoice_total_days
      ,hire_weekdays
      ,[DerivedRateType]
      ,invoice_line_quantity
      ,invoice_branch_division_code
      ,invoice_branch_code
      ,hire_line_rate1
      ,make_model_current_caps_rate1
      ,invoice_tran_id
      ,flt.fleet_no
      ,sub.Pricing_Group
      ,sub.Sub_Type_V1
      ,sub.Sub_Type_V2
      ,[invoice_line_rate1]
      ,[invoice_line_rate2]
  INTO [scratch].[dbo].[pricing_clustering_invoice_detail]
  FROM [extract_snapshots].[pricing].[invoice_details] inv
  INNER JOIN [PROD_OLTP].[natlive].[dbo].[FLEET_FleetMaster] flt
        ON   inv.fleet_no = flt.fleet_no
  INNER JOIN [scratch].[dbo].[pricing_clustering_subtype] sub
        ON   flt.[PricingGroup] = sub.[Pricing_Group]
  where fleet_is_common_numbered='no'
      and hire_created_by_name != 'Chase.Convert'
      and hire_line_is_subhire = 'no'
      and (hire_line_subhire_creditor_code is NULL or hire_line_subhire_creditor_code = '')
      and hire_line_type_description = 'hire'
      and (hire_line_package_name is NULL or hire_line_package_name ='')
      and (hire_line_flat_monthly_charge = 0.00 or hire_line_flat_monthly_charge is NULL)
      and hire_line_block_charge_indicator = 'N'
      ----remove by Gordon
      --and customer_tendered_flag = 'no'
      and invoice_type = 'I'
      and invoice_posted_date >= '2011-02-01' and invoice_posted_date<='2013-01-31'
      and invoice_branch_division_code = 'east'
      and invoice_line_quantity >=1
      and invoice_line_billable_days >= 1
      ----and invoice_branch_code not in branch speciallist  --add later
      ---- add by Gordon
      and hire_line_rate1 >= (0.1 *[make_model_current_caps_rate1])
      and hire_line_rate1 <= (2 *[make_model_current_caps_rate1]) 
      and [customer_code] not like '%CASH%'
--12343143124
      
--drop table [scratch].[dbo].[invoice_details_clustering]
select top 1000 * from [scratch].[dbo].[pricing_clustering_invoice_detail]
select count(*) from [scratch].[dbo].[pricing_clustering_invoice_detail]
