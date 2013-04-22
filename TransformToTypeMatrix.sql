
	-- 1. Aggregate the rows via group by hire_no + hire_line_no, and filter customer with revenue in (5k~150k)
	SELECT A.customer_code, A.Sub_Type_V2, -- cast(A.fleet_type_code as int) as fleet_type_code, 
			sum(B.revenue) as revenue
	INTO #TMP_HIRE_LINE
	FROM
	(
		  [scratch].[dbo].[pricing_clustering_invoice_detail] A
		  INNER JOIN
		  (
				SELECT * FROM
				(
					SELECT hire_no as hire_no, hire_line_no as hire_line_no,
							sum(invoice_line_total_exGST) as revenue, 
							min(invoice_tran_id) as invoice_tran_id
					FROM [scratch].[dbo].[pricing_clustering_invoice_detail]
					GROUP BY hire_no, hire_line_no
				) r WHERE r.revenue > 0 -- remove the hire line whose revenue is zero
		  )B ON A.hire_no = B.hire_no 
			  and A.hire_line_no = B.hire_line_no 
			  and A.invoice_tran_id = B.invoice_tran_id
		  INNER JOIN 
		  (
				SELECT * FROM
				(
					SELECT customer_code, sum([invoice_line_total_exGST]) as cust_revenue
					FROM [scratch].[dbo].[pricing_clustering_invoice_detail]
					group by customer_code
				) p WHERE p.cust_revenue >= 5000 AND p.cust_revenue <= 150000 -- select the middle customer
		  )C ON A.customer_code = C.customer_code
	) 
	group by A.customer_code, A.Sub_Type_V2--A.fleet_type_code
	order by A.customer_code, A.Sub_Type_V2--A.fleet_type_code
	
	-- 2. prepare the count data for hire lines, and order by desc.
    SELECT customer_code, count(*) as cnt_hire
    INTO #TMP_CNT_HIRE_LINE
    FROM #TMP_HIRE_LINE
    GROUP BY customer_code
    ORDER BY cnt_hire DESC
    
    -- 3. filter the company who occupied 90% in count of hire lines.
		---- 3.1 get total count of hire lines
		DECLARE @tot_cnt_hire float	  
		SET @tot_cnt_hire = (SELECT sum(cnt_hire) FROM #TMP_CNT_HIRE_LINE)
		--SELECT @tot_cnt_hire
		
		---- 3.2 init variables and temp table.
		DECLARE @acc_cnt_hire INT
		DECLARE @cust_code VARCHAR(100)
		DECLARE @cnt_hire INT
	    
		SET @acc_cnt_hire = 0
		CREATE TABLE #TMP_BIG_COMPANY
		(
			customer_code varchar(200),
			cnt_hire  int
		)
	    
		---- 3.3 using cursor to fetch the big company, and they are ordered in previous section.
		DECLARE myCursor CURSOR FOR
			SELECT customer_code, cnt_hire
			FROM #TMP_CNT_HIRE_LINE
		OPEN myCursor;
		FETCH NEXT FROM myCursor
		INTO @cust_code, @cnt_hire;
		
		WHILE @@FETCH_STATUS = 0
		BEGIN
			SET @acc_cnt_hire = @acc_cnt_hire + @cnt_hire
			
			IF (@acc_cnt_hire / @tot_cnt_hire < 0.9) -- record this company as big company
				INSERT INTO #TMP_BIG_COMPANY VALUES(@cust_code, @cnt_hire)
			ELSE
				BREAK
						
			FETCH NEXT FROM myCursor
			INTO @cust_code, @cnt_hire;
		END
			
		CLOSE myCursor;
		DEALLOCATE myCursor;
		
	-- 4. Create the new table to hold the new data, one for revenue, one for all
	USE scratch
	IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES 
			     WHERE TABLE_TYPE='BASE TABLE' 
			     AND TABLE_CATALOG = 'scratch'
			     AND TABLE_NAME='pricing_clustering_revenue')
		DROP TABLE [scratch].[dbo].[pricing_clustering_revenue]
			
	SELECT p.*, t.cnt_hire
	INTO [scratch].[dbo].[pricing_clustering_revenue]
	FROM #TMP_BIG_COMPANY t
		INNER JOIN #TMP_HIRE_LINE p ON t.customer_code = p.customer_code
    
	USE scratch
	IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES 
			     WHERE TABLE_TYPE='BASE TABLE' 
			     AND TABLE_CATALOG = 'scratch'
			     AND TABLE_NAME='pricing_clustering_invoice_detail_filtered')
		DROP TABLE [scratch].[dbo].[pricing_clustering_invoice_detail_filtered]
		
    SELECT A.*
    INTO [scratch].[dbo].[pricing_clustering_invoice_detail_filtered]
    FROM [scratch].[dbo].[pricing_clustering_invoice_detail] A
		  INNER JOIN 
		  (
				SELECT DISTINCT customer_code AS big_customer_code
				FROM [scratch].[dbo].[pricing_clustering_revenue]
		  ) f ON A.customer_code = f.big_customer_code 
    
	USE scratch
	IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES 
			     WHERE TABLE_TYPE='BASE TABLE' 
			     AND TABLE_CATALOG = 'scratch'
			     AND TABLE_NAME='pricing_clustering_rev_cust')
		DROP TABLE [scratch].[dbo].[pricing_clustering_rev_cust]
	
	-- nomalized the revenue	
    SELECT A.*, B.tot_revenue, A.revenue/B.tot_revenue AS subtype_weight
    INTO [scratch].[dbo].[pricing_clustering_rev_cust]
    FROM #TMP_HIRE_LINE A
		  INNER JOIN
		  (
				SELECT customer_code, sum(revenue) as tot_revenue
				FROM #TMP_HIRE_LINE
				GROUP BY customer_code
		  ) B
		  ON A.customer_code = B.customer_code
		  
	-- 5. Drop tmp tables.
	DROP TABLE #TMP_CNT_HIRE_LINE
	DROP TABLE #TMP_HIRE_LINE
	DROP TABLE #TMP_BIG_COMPANY
	
	-- 6. show the result
    SELECT top 1000 customer_code as cc, fleet_type_code as tc, * FROM [scratch].[dbo].[pricing_clustering_invoice_detail_filtered]
    order by customer_code, fleet_type_code
    
    SELECT count(*) FROM [scratch].[dbo].[pricing_clustering_invoice_detail_filtered]
    
