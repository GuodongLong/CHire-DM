-- ================================================
-- Template generated from Template Explorer using:
-- Create Procedure (New Menu).SQL
--
-- Use the Specify Values for Template Parameters 
-- command (Ctrl-Shift-M) to fill in the parameter 
-- values below.
--
-- This block of comments will not be included in
-- the definition of the procedure.
-- ================================================
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
CREATE PROCEDURE [dbo].[pricing_clustering_filter]
	-- Add the parameters for the stored procedure here
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
    ;WITH pricing_clustering AS(
		SELECT * from [scratch].[dbo].[invoice_details_clustering]
	)	  
	
	-- 1. Aggregate the rows via group by hire_no + hire_line_no, and filter customer with revenue in (5k~150k)
	SELECT c.customer_code, cast(c.fleet_type_code as int) as fleet_type_code, sum(c.revenue) as revenue, c.cnt_hires
	INTO #TMP_HIRE_LINE
	FROM
	(
	  SELECT * FROM
	  (    
			SELECT hire_no as hn, hire_line_no as hln,sum([invoice_line_total_exGST]) as revenue, min([invoice_tran_id]) as itd
			FROM [scratch].[dbo].[invoice_details_clustering]
			group by hire_no, hire_line_no
	  ) a  
	  INNER JOIN 
	  (
			SELECT * FROM
			(
				SELECT sum([invoice_line_total_exGST]) as revenue
				FROM [scratch].[dbo].[invoice_details_clustering]
				group by customer_code
			) WHERE revenue >= 5000 AND revenue <= 150000
	  ) b
	  ON 
	  a.hn = b.hire_no 
	  and a.hln = b.hire_line_no 
	  and a.itd = b.[invoice_tran_id]
	) c
	group by c.customer_code,c.fleet_type_code
	order by customer_code, fleet_type_code

	-- 2. prepare the count data for hire lines, and order by desc.
    SELECT customer_code, count(*) as cnt_hire
    INTO #TMP_CNT_HIRE_LINE
    FROM (SELECT * FROM #TMP_HIRE_LINE ORDER BY cnt_hire DESC)
    GROUP BY customer_code
    ORDER BY cnt_hire DESC
    
    -- 3. filter the company occupied the 90% in count of hire lines.
		---- 3.1 get total count of hire lines
		DECLARE @tot_cnt_hire float	  
		SET @tot_cnt_hire = (SELECT sum(cnt_hire) FROM #TMP_CNT_HIRE_LINE
		SELECT @tot_cnt_hire
		
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
			FROM #TMP_HIRE_LINE
			
		WHILE @@FETCH_STATUS = 0
		BEGIN
			FETCH NEXT INTO @cust_code, @cnt_hire FROM myCursor;
			SET @acc_cnt_hire = @acc_cnt_hire + @cnt_hire
			
			IF (@acc_cnt_hire / @tot_cnt_hire < 0.9) -- record this company as big company
				INSERT INTO #TMP_BIG_COMPANY (@cust_code, @cnt_hire)
			ELSE
				BREAK
		END
			
		CLOSE myCursor;
		DEALLOCATE myCursor;
	
	-- 4. Create the new table to hold the new data
	SELECT t.cnt_hire, p.*
	INTO [scratch].[dbo].[invoice_details_clustering_filtered]
	FROM #TMP_BIG_COMPANY t
		INNER JOIN [scratch].[dbo].[invoice_details_clustering] p ON t.customer_code = p.customer_code

	-- 5. Drop tmp tables.
	DROP TABLE #TMP_CNT_HIRE_LINE
	DROP TABLE #TMP_HIRE_LINE
	DROP TABLE #TMP_BIG_COMPANY
END
GO
