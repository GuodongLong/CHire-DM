-- 1. generate original table
USE scratch
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES 
		     WHERE TABLE_TYPE='BASE TABLE' 
		     AND TABLE_CATALOG = 'scratch'
		     AND TABLE_NAME='pricing_clustering_rate')
	DROP TABLE [scratch].[dbo].[pricing_clustering_rate]


SELECT A.customer_code, --CAST(A.fleet_type_code as int) as fleet_type_code, 
	   A.Sub_Type_V2,
	   SUM(invoice_line_total_exGST) AS revenue, 
	   SUM(volume) AS tot_volume,
	   CASE
		WHEN SUM(daily_rate_cnt) > 0
			THEN SUM(rate1)/SUM(volume)
			ELSE 0
		END AS daily_rate, 
	   CASE
		WHEN SUM(weekly_rate_cnt) > 0
			THEN SUM(rate2)/SUM(volume)
			ELSE 0
		END AS weekly_rate, 
	   CASE
		WHEN SUM(fortnight_rate_cnt) > 0
			THEN SUM(rate2)/SUM(volume)
			ELSE 0
		END AS fortnight_rate, 
	   CASE
		WHEN SUM(monthly_rate_cnt) > 0
			THEN SUM(rate2)/SUM(volume)
			ELSE 0
		END AS monthly_rate, 
	   CASE
		WHEN SUM(monthly2_rate_cnt) > 0
			THEN SUM(rate2)/SUM(volume)
			ELSE 0
		END AS monthly2_rate
INTO [scratch].[dbo].[pricing_clustering_rate]
FROM
(
		  [scratch].[dbo].[pricing_clustering_invoice_detail_filtered] A
		  INNER JOIN
		  (
				SELECT  *,
						CASE WHEN duration <= 4
								THEN volume
								ELSE 0	
						END as daily_rate_cnt,
						CASE WHEN duration >= 5 
								AND duration < 2*hire_weekdays
								THEN volume
								ELSE 0	
						END as weekly_rate_cnt,
						CASE WHEN duration >= 2*hire_weekdays
								AND duration < 4*hire_weekdays
								THEN volume
								ELSE 0	
						END as fortnight_rate_cnt,
						CASE WHEN duration >= 4*hire_weekdays
								AND duration < 8*hire_weekdays
								THEN volume
								ELSE 0	
						END as monthly_rate_cnt,
						CASE WHEN duration >= 8*hire_weekdays
								THEN volume
								ELSE 0	
						END as monthly2_rate_cnt
				FROM 
				(
					SELECT hire_no, hire_line_no, 
						min(invoice_tran_id) as invoice_tran_id,
						max(hire_weekdays) as hire_weekdays,
						sum(invoice_line_total_exGST) as revenue, 
						max(hire_line_invoice_total_days) as duration,
				        max(invoice_line_quantity) * max(hire_shift) as volume,
				        max(invoice_line_rate1) * max(invoice_line_quantity) * max(hire_shift) as rate1,
				        max(invoice_line_rate2) * max(invoice_line_quantity) * max(hire_shift) as rate2
					FROM [scratch].[dbo].[pricing_clustering_invoice_detail_filtered]
					GROUP BY hire_no, hire_line_no
				) g
				WHERE g.revenue > 0 AND g.hire_weekdays > 0 AND g.duration > 0 AND volume > 0 AND rate1 > 0 AND rate2 > 0  
		  )B ON A.hire_no = B.hire_no 
			  and A.hire_line_no = B.hire_line_no 
			  and A.invoice_tran_id = B.invoice_tran_id
)
GROUP BY A.customer_code, A.Sub_Type_V2--A.fleet_type_code
ORDER BY A.customer_code, A.Sub_Type_V2--A.fleet_type_code

-- 2. generate using subtype list
USE scratch
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES 
		     WHERE TABLE_TYPE='BASE TABLE' 
		     AND TABLE_CATALOG = 'scratch'
		     AND TABLE_NAME='pricing_clustering_rate_subtype_list')
	DROP TABLE [scratch].[dbo].[pricing_clustering_rate_subtype_list]
--SELECT * FROM [scratch].[dbo].[pricing_clustering_rate]
CREATE TABLE [scratch].[dbo].[pricing_clustering_rate_subtype_list]
(
	subtype_id int IDENTITY(1,1)PRIMARY KEY CLUSTERED,
	subtype varchar(50)
)
INSERT INTO pricing_clustering_rate_subtype_list (subtype)
SELECT DISTINCT Sub_Type_V2 as subtype 
FROM [scratch].[dbo].[pricing_clustering_rate]

-- 3. convert subtype into numeric id
USE scratch
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES 
		     WHERE TABLE_TYPE='BASE TABLE' 
		     AND TABLE_CATALOG = 'scratch'
		     AND TABLE_NAME='pricing_clustering_rate_final')
	DROP TABLE [scratch].[dbo].[pricing_clustering_rate_final]
SELECT A.*, B.subtype_id 
INTO [scratch].[dbo].[pricing_clustering_rate_final]
FROM [scratch].[dbo].[pricing_clustering_rate] A
	INNER JOIN [scratch].[dbo].[pricing_clustering_rate_subtype_list] B
	ON A.Sub_Type_V2 = B.subtype

	
-- 4. Generate average rate for each subtype
SELECT Sub_Type_V2, sum(invoice_line_total_exGST/(hire_line_invoice_total_days*invoice_line_quantity*hire_shift)) AS avg_rate
FROM [scratch].[dbo].[pricing_clustering_invoice_detail_filtered] A
GROUP BY Sub_Type_V2

SELECT F.customer_code, F.subtype_id
	   ,F.daily_rate, F.weekly_rate, F.fortnight_rate, F.monthly_rate, F.monthly2_rate
	   ,R.subtype_weight, B.avg_daily_rate, B.avg_weekly_rate
FROM [scratch].[dbo].[pricing_clustering_rate_final] F
		INNER JOIN [scratch].[dbo].[pricing_clustering_rev_cust] R
		ON R.customer_code = F.customer_code AND R.sub_type_v2 = F.Sub_Type_V2
		INNER JOIN
		(
			SELECT Sub_Type_V2
				,avg([invoice_line_rate1]) as avg_daily_rate
				,avg([invoice_line_rate2]) as avg_weekly_rate
			FROM [scratch].[dbo].[pricing_clustering_invoice_detail_filtered]
			GROUP BY Sub_Type_V2
		) B ON B.sub_type_v2 = R.sub_type_v2
ORDER BY customer_code, subtype_id

SELECT max(subtype_id) from [scratch].[dbo].[pricing_clustering_rate_final]