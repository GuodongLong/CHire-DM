--/****** Script for SelectTopNRows command from SSMS  ******/
--SELECT TOP 100 [customer_code],* from 
-- [coa-bi-prod01\prod_dw].[extract_snapshots].[pricing].[invoice_details] 
--where [customer_code] = 'ELGL6000'
  
  --SELECT TOP 1000 * from 
  --   Natlive_Ext.dbo.gps_cluster_customer 
 
  
  select 
   cluster.clusterid
  ,cluster.customerCode
  ,sum(inv.invoice_line_total_exGST) as revenue
  ,count(*) invoice_count
  
      --into Natlive_Ext.dbo.cluster_revenue
 
   from Natlive_Ext.dbo.gps_cluster_customer cluster
        inner join  [coa-bi-prod01\prod_dw].[extract_snapshots].[pricing].[invoice_details] inv 
            on cluster.customerCode = inv.customer_code COLLATE Latin1_General_CI_AS
      --where cluster.customerCode= 'ELGL6000'  
            group by cluster.clusterid
              ,cluster.customerCode
      
