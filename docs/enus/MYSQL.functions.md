## GETDATE()
```sql
CREATE FUNCTION GETDATE() 
RETURNS datetime
    DETERMINISTIC
begin
	return now();
end
```

## CHARINDEX 
```sql
CREATE FUNCTION CHARINDEX(
	  findstr varchar(1000)
	, sourcestr text
) RETURNS int
    DETERMINISTIC
begin
	/* Compatible with SQL Server syntax */
	return instr(sourcestr, findstr);	
END
```

## fn_chn_money  Convert currency to Chinese uppercase, from SQL Server stored procedure
```sql
CREATE FUNCTION fn_chn_money (
	in_ChangeMoney numeric(10,2)
) RETURNS varchar(500) CHARSET utf8mb4
    DETERMINISTIC
begin
	/** Convert currency to Chinese uppercase, from SQL Server stored procedure **/
	Declare    p_ReturnValue    VarChar(500);

    Declare    p_String1    char(20);
    Declare    p_String2    char(30);
    Declare    p_String4    Varchar(100);
    Declare p_String3     Varchar(100);    --  Value taken from original A
    Declare p_i         int;              --  Loop variable
    Declare p_J         Int;              --  Length of string after A value multiplied by 100
    Declare p_Ch1         Varchar(100);   --  Chinese reading of the digit
    Declare p_Ch2         Varchar(100) ;  --  Chinese reading of the digit position
    Declare p_Zero         Int  ;         --  Used to count consecutive zeros
    
	Declare p_ChangeMoney1 numeric(10,2);
	declare p_ttt varchar(200);

	declare p_temp int;
	set	p_ChangeMoney1 = (in_ChangeMoney);
	
	if (in_ChangeMoney < 0) then
		set	in_ChangeMoney = (in_ChangeMoney * -1);
	end if;
	
    set  p_ReturnValue = '';
    set  p_String1 = '零壹贰叁肆伍陆柒捌玖';
    set  p_String2 = '万仟佰拾亿仟佰拾万仟佰拾元角分';

    set p_String4 = cast(Cast(in_ChangeMoney * 100 as SIGNED) as CHAR);    

    set p_J= length(p_String4);

    set p_String2=Right(p_String2,p_J);

    set    p_i = 1 ;

    while    p_i<= p_j DO

        set p_String3 = Substring(p_String4, p_i, 1);

        if p_String3 <> '0' then

            set    p_Ch1 = Substring(p_String1, Cast(p_String3 as SIGNED) + 1, 1);
           
            set    p_Ch2 = Substring(p_String2, p_i, 1);
            set    p_Zero = 0;                    -- Indicates the current digit is not zero
            
           
        else 
            If (p_Zero = 0) Or (p_i = p_J - 9) Or (p_i = p_J - 5) Or (p_i = p_J - 1) then
                        set p_Ch1 = '零' ;
                    Else
                        set p_Ch1 = '';
			end if;
            
		    set p_Zero = p_Zero + 1    ;         -- Indicates the current digit is 0
                        
            -- If the conversion value needs to be expanded, modify the I value in the expression below.
            set p_Ch2 = '';

            If p_i = p_J - 10  then
                        set p_Ch2 = '亿';
                        set p_Zero = 0;
            end if;
                    
            If p_i = p_J - 6 then
                        set p_Ch2 = '万';
                        set p_Zero = 0;
            end if;
                    
            if p_i = p_J - 2 then
                        set p_Ch2 = '元';
                        set p_Zero = 0;
            end if;
                    
            If p_i = p_J then 
                        set p_Ch2 = '整';
            end if;            
        end if;
 
        set p_ReturnValue = concat( p_ReturnValue , p_Ch1 , p_Ch2);

        set p_i = p_i + 1;
    end while;
-- set p_temp = Cast('XXX' as SIGNED);
   
    -- Finally remove redundant zeros
    If CharIndex('仟仟',p_ReturnValue) <> 0 then 
            set p_ReturnValue = Replace(p_ReturnValue, '仟仟', '仟');
    end if;        

    If CharIndex('佰佰',p_ReturnValue) <> 0 then 
            set p_ReturnValue = Replace(p_ReturnValue, '佰佰', '佰');
    end if;       

    If CharIndex('零元',p_ReturnValue) <> 0 then 
        set p_ReturnValue = Replace(p_ReturnValue, '零元', '元');
 	end if;       
    If CharIndex('零万',p_ReturnValue) <> 0 then
        set p_ReturnValue = Replace(p_ReturnValue, '零万', '万');
	end if; 
    If CharIndex('零亿',p_ReturnValue) <> 0 then
        set p_ReturnValue = Replace(p_ReturnValue, '零亿', '亿');
	end if; 
    If CharIndex('零整',p_ReturnValue) <> 0  then
        set p_ReturnValue = Replace(p_ReturnValue, '零整', '整');
    end if;
    If CharIndex('零佰',p_ReturnValue) <> 0  then
            set p_ReturnValue = Replace(p_ReturnValue, '零佰', '零');
 	end if;
    If CharIndex('零仟',p_ReturnValue) <> 0 then
            set p_ReturnValue = Replace(p_ReturnValue, '零仟', '零');
	end if;
    If CharIndex('元元',p_ReturnValue) <> 0 then
            set p_ReturnValue = Replace(p_ReturnValue, '元元', '元');
	end if;
	if p_ChangeMoney1 < 0 then
			set p_j = length(p_ReturnValue);
			set p_ttt	= right(p_ReturnValue,p_j) ;
			return '负'+p_ReturnValue;
	end if;
	IF p_ReturnValue='整' then
		SET p_ReturnValue='零元';
	end if;

    return p_ReturnValue;
end
```

## f_getpy  Get Chinese Pinyin, first letter
```sql
CREATE f_getPY(
	PARAM VARCHAR(255)
) RETURNS varchar(2) CHARSET utf8mb4
    DETERMINISTIC
BEGIN  
    DECLARE V_RETURN VARCHAR(255);  
    DECLARE V_FIRST_CHAR VARCHAR(2);  
    SET V_FIRST_CHAR = UPPER(LEFT(PARAM,1));  
    SET V_RETURN = V_FIRST_CHAR;  
    IF LENGTH( V_FIRST_CHAR) <> CHARACTER_LENGTH( V_FIRST_CHAR ) THEN  
    SET V_RETURN = ELT(INTERVAL(CONV(HEX(LEFT(CONVERT(PARAM USING gbk),1)),16,10),  
        0xB0A1,0xB0C5,0xB2C1,0xB4EE,0xB6EA,0xB7A2,0xB8C1,0xB9FE,0xBBF7,  
        0xBFA6,0xC0AC,0xC2E8,0xC4C3,0xC5B6,0xC5BE,0xC6DA,0xC8BB,  
        0xC8F6,0xCBFA,0xCDDA,0xCEF4,0xD1B9,0xD4D1),  
    'A','B','C','D','E','F','G','H','J','K','L','M','N','O','P','Q','R','S','T','W','X','Y','Z');  
    END IF;  
    RETURN V_RETURN;  
END
```

## fn_GetQuanPin  Get full pinyin, requires bas_pin_yin table
```sql
CREATE FUNCTION fn_GetQuanPin(
	NAME VARCHAR(255) CHARSET gbk
) RETURNS varchar(255) CHARSET gbk
    DETERMINISTIC
BEGIN
    DECLARE mycode INT;
    DECLARE tmp_lcode VARCHAR(2) CHARSET gbk;
    DECLARE lcode INT;
    DECLARE tmp_rcode VARCHAR(2) CHARSET gbk;
    DECLARE rcode INT;
    DECLARE mypy VARCHAR(255) CHARSET gbk DEFAULT '';
    DECLARE lp INT;
    SET mycode = 0;
    SET lp = 1;
    SET NAME = HEX(NAME);
    WHILE lp < LENGTH(NAME) DO
        SET tmp_lcode = SUBSTRING(NAME, lp, 2);
        SET lcode = CAST(ASCII(UNHEX(tmp_lcode)) AS UNSIGNED); 
        SET tmp_rcode = SUBSTRING(NAME, lp + 2, 2);
        SET rcode = CAST(ASCII(UNHEX(tmp_rcode)) AS UNSIGNED); 
        IF lcode > 128 THEN
            SET mycode =65536 - lcode * 256 - rcode ;
            SELECT CONCAT(mypy,pin_yin_) INTO mypy FROM oa_meta.bas_pin_yin WHERE CODE_ >= ABS(mycode) ORDER BY CODE_ ASC LIMIT 1;
            SET lp = lp + 4;
        ELSE
            SET mypy = CONCAT(mypy,CHAR(CAST(ASCII(UNHEX(SUBSTRING(NAME, lp, 2))) AS UNSIGNED)));
            SET lp = lp + 2;
        END IF;
    END WHILE;
    RETURN UPPER(mypy);
END
```

## FN_ZERO_ADD  Pad number to fixed-length string with leading zeros
```sql
CREATE FN_ZERO_ADD (
	  IN_NUM INT
	, IN_LEN INT
) RETURNS varchar(1000) CHARSET utf8mb4
    DETERMINISTIC
BEGIN
	DECLARE P_V VARCHAR(2000);
	SET @V= CONCAT(replace(space(IN_LEN),' ','0'), IN_NUM);
	RETURN RIGHT(@V,IN_LEN);
END
```
