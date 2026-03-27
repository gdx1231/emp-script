#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SQL 文件数据脱敏脚本 - 替换名称、电话、email、地址为随机数据
"""

import random
import re

# 固定种子以保证可重复性
random.seed(123456)

# 中文姓氏
SURNAMES = ["赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈", "褚", "卫",
        "蒋", "沈", "韩", "杨", "朱", "秦", "尤", "许", "何", "吕", "施", "张", "孔",
        "曹", "严", "华", "金", "魏", "陶", "姜", "戚", "谢", "邹", "喻", "柏", "水",
        "窦", "章", "云", "苏", "潘", "葛", "奚", "范", "彭", "郎", "鲁", "韦", "昌",
        "马", "苗", "凤", "花", "方", "俞", "任", "袁", "柳", "酆", "鲍", "史", "唐",
        "费", "廉", "岑", "薛", "雷", "贺", "倪", "汤", "滕", "殷", "罗", "毕", "郝",
        "邬", "安", "常", "乐", "于", "时", "傅", "皮", "卞", "齐", "康", "伍", "余",
        "元", "卜", "顾", "孟", "平", "黄", "和", "穆", "萧", "尹", "姚", "邵", "湛",
        "汪", "祁", "毛", "禹", "狄", "米", "贝", "明", "臧", "计", "伏", "成", "戴",
        "谈", "宋", "茅", "庞", "熊", "纪", "舒", "屈", "项", "祝", "董", "梁", "杜",
        "阮", "蓝", "闵", "席", "季", "麻", "强", "贾", "路", "娄", "危", "江", "童",
        "颜", "郭", "梅", "盛", "林", "刁", "钟", "徐", "邱", "骆", "高", "夏", "蔡",
        "田", "樊", "胡", "凌", "霍", "虞", "万", "支", "柯", "昝", "管", "卢", "莫",
        "经", "房", "裘", "缪", "干", "解", "应", "宗", "丁", "宣", "奔", "邓", "郁",
        "单", "杭", "洪", "包", "诸", "左", "石", "崔", "吉", "钮", "龚", "程", "嵇",
        "邢", "滑", "裴", "陆", "荣", "翁", "荀", "羊", "於", "惠", "甄", "曲", "家",
        "封", "芮", "羿", "储", "靳", "汲", "邴", "糜", "松", "井", "段", "富", "巫",
        "乌", "焦", "巴", "弓", "牧", "隗", "山", "谷", "车", "侯", "宓", "蓬", "全",
        "郗", "班", "仰", "秋", "仲", "伊", "宫", "宁", "仇", "栾", "暴", "甘", "钭",
        "厉", "戎", "祖", "武", "符", "刘", "景", "詹", "束", "龙", "叶", "幸", "司",
        "韶", "郜", "黎", "蓟", "薄", "印", "宿", "白", "怀", "蒲", "邰", "从", "鄂",
        "索", "咸", "籍", "赖", "卓", "蔺", "屠", "蒙", "池", "乔", "阴", "郁", "胥",
        "能", "苍", "双", "闻", "莘", "党", "翟", "谭", "贡", "劳", "逄", "姬", "申",
        "扶", "堵", "冉", "宰", "郦", "雍", "却", "璩", "桑", "桂", "濮", "牛", "寿",
        "通", "边", "扈", "燕", "冀", "郏", "浦", "尚", "农", "温", "别", "庄", "晏",
        "柴", "瞿", "阎", "充", "慕", "连", "茹", "习", "宦", "艾", "鱼", "容", "向",
        "古", "易", "慎", "戈", "廖", "庾", "终", "暨", "居", "衡", "步", "都", "耿",
        "满", "弘", "匡", "国", "文", "寇", "广", "禄", "阙", "东", "欧", "殳", "沃",
        "利", "蔚", "越", "夔", "隆", "师", "巩", "厍", "聂", "晁", "勾", "敖", "融",
        "冷", "訾", "辛", "阚", "那", "简", "饶", "空", "曾", "毋", "沙", "乜", "养",
        "鞠", "须", "丰", "巢", "关", "蒯", "相", "查", "后", "荆", "红", "游", "竺",
        "权", "逯", "盖", "益", "桓", "公"]

# 中文名字
GIVEN_NAMES_MALE = ["伟", "刚", "勇", "毅", "俊", "峰", "强", "军", "平", "保", "东",
        "文", "辉", "力", "明", "永", "健", "世", "广", "志", "义", "兴", "良", "海",
        "山", "仁", "波", "宁", "贵", "福", "生", "龙", "元", "全", "国", "胜", "学",
        "祥", "才", "发", "武", "新", "利", "清", "飞", "彬", "富", "顺", "信", "子",
        "杰", "涛", "昌", "成", "康", "星", "光", "天", "达", "安", "岩", "中", "茂",
        "进", "林", "有", "坚", "和", "彪", "博", "诚", "先", "敬", "震", "振", "壮",
        "会", "思", "群", "豪", "心", "邦", "承", "乐", "绍", "功", "松", "善", "厚",
        "庆", "磊", "民", "友", "裕", "河", "哲", "江", "超", "浩", "亮", "政", "谦",
        "亨", "奇", "固", "之", "轮", "翰", "朗", "伯", "宏", "言", "若", "鸣", "朋",
        "斌", "梁", "栋", "维", "启", "克", "伦", "翔", "旭", "鹏", "泽", "晨", "辰",
        "士", "以", "建", "家", "致", "树", "炎", "德", "行", "时", "泰", "盛", "雄",
        "琛", "钧", "冠", "策", "腾", "楠", "榕", "风", "航", "弘"]

GIVEN_NAMES_FEMALE = ["秀", "娟", "英", "华", "慧", "巧", "美", "娜", "静", "淑", "惠",
        "珠", "翠", "雅", "芝", "玉", "萍", "红", "娥", "玲", "芬", "芳", "燕", "彩",
        "春", "菊", "兰", "凤", "洁", "梅", "琳", "素", "云", "莲", "真", "环", "雪",
        "荣", "爱", "妹", "霞", "香", "月", "莺", "媛", "艳", "瑞", "凡", "佳", "嘉",
        "琼", "勤", "珍", "贞", "莉", "桂", "娣", "叶", "璧", "璐", "娅", "琦", "晶",
        "妍", "茜", "秋", "珊", "莎", "锦", "黛", "青", "倩", "婷", "姣", "婉", "娴",
        "瑾", "颖", "露", "瑶", "怡", "婵", "雁", "蓓", "纨", "仪", "荷", "丹", "蓉",
        "眉", "君", "琴", "蕊", "薇", "菁", "梦", "岚", "苑", "婕", "馨", "瑗", "琰",
        "韵", "融", "园", "艺", "咏", "卿", "聪", "澜", "纯", "毓", "悦", "昭", "冰",
        "爽", "琬", "茗", "羽", "希", "宁", "欣", "飘", "育", "滢", "馥", "筠", "柔",
        "竹", "霭", "凝", "晓", "欢", "霄", "枫", "芸", "菲", "寒", "伊", "亚", "宜",
        "可", "姬", "舒", "影", "荔", "丽", "秀", "韵", "晴", "画", "屏", "妩", "妤"]

# 城市列表
CITIES = ["北京", "上海", "广州", "深圳", "成都", "杭州", "重庆", "武汉", "西安",
        "南京", "天津", "苏州", "郑州", "长沙", "沈阳", "青岛", "宁波", "无锡",
        "大连", "东莞", "佛山", "福州", "昆明", "合肥", "哈尔滨", "长春", "石家庄",
        "济南", "南宁", "南昌", "贵阳", "太原", "厦门", "温州", "烟台", "嘉兴",
        "保定", "珠海", "中山", "惠州", "廊坊", "汕头", "三亚", "海口"]

# 区县级地名
DISTRICTS = ["朝阳区", "海淀区", "西城区", "东城区", "丰台区", "石景山区", "门头沟区",
        "房山区", "通州区", "顺义区", "昌平区", "大兴区", "怀柔区", "平谷区",
        "密云区", "延庆区", "浦东新区", "黄浦区", "徐汇区", "长宁区", "静安区",
        "普陀区", "虹口区", "杨浦区", "闵行区", "宝山区", "嘉定区", "金山区",
        "松江区", "青浦区", "奉贤区", "崇明区", "天河区", "越秀区", "海珠区",
        "荔湾区", "白云区", "黄埔区", "花都区", "番禺区", "南沙区", "增城区",
        "从化区", "福田区", "罗湖区", "南山区", "宝安区", "龙岗区", "盐田区",
        "龙华区", "坪山区", "光明区"]

# 街道名称
STREETS = ["中山路", "人民路", "解放路", "建设路", "和平路", "友谊路", "文化路",
        "光明路", "幸福路", "胜利路", "振兴路", "发展大道", "创业路", "科技路",
        "创新路", "花园路", "迎宾路", "朝阳路", "黄河路", "长江路", "珠江路",
        "淮河路", "太湖路", "洞庭湖路", "鄱阳湖路", "青海湖路", "西湖路", "东湖路",
        "南湖路", "北湖路", "中湖路", "环城路", "二环", "三环", "四环", "五环",
        "机场路", "高速路", "快速路", "步行街", "商业街", "金融街", "美食街",
        "酒吧街", "古玩街", "花鸟街"]

# 门牌号前缀
BUILDING_TYPES = ["号院", "号楼", "大厦", "中心", "广场", "花园", "小区", "公寓",
        "写字楼", "商务楼", "国际", "家园", "新村", "别墅", "酒店", "宾馆",
        "商厦", "市场", "园区", "基地"]

# 公司名前缀
COMPANY_PREFIXES = ["北京", "上海", "广州", "深圳", "中国", "国际", "华夏", "东方",
        "南方", "北方", "中原", "神州", "环球", "亚太", "东亚", "西亚", "北欧",
        "中欧", "东欧", "泛美", "太平洋", "大西洋", "印度洋", "昆仑", "长城",
        "黄河", "长江", "泰山", "华山", "衡山", "恒山", "嵩山", "黄山", "庐山",
        "峨眉山", "武当山", "喜马拉雅", "天山", "阿尔泰山", "祁连山"]

# 公司名行业词
COMPANY_INDUSTRIES = ["科技", "技术", "信息", "网络", "软件", "系统", "数码", "电子",
        "通信", "电信", "移动", "联通", "铁通", "广电", "传媒", "广告", "文化",
        "教育", "培训", "咨询", "管理", "投资", "金融", "银行", "保险", "证券",
        "基金", "信托", "租赁", "贸易", "商贸", "进出口", "物流", "运输", "快递",
        "仓储", "供应链", "实业", "工业", "制造", "生产", "加工", "机械", "机电",
        "汽车", "配件", "建材", "装饰", "装修", "房地产", "物业", "酒店", "餐饮",
        "旅游", "旅行社", "航空", "票务", "医药", "医疗", "器械", "生物", "制药",
        "化工", "能源", "电力", "石油", "天然气", "煤炭", "矿业", "冶金", "钢铁",
        "有色", "稀土", "新材料", "新能源", "环保", "节能", "农业", "林业", "牧业",
        "渔业", "食品", "饮料", "纺织", "服装", "皮革", "家具", "家电", "日用品",
        "化妆品", "珠宝", "首饰", "工艺品", "礼品", "玩具", "文体", "用品", "图书",
        "音像", "影视", "娱乐", "体育", "健身", "美容", "美发", "保健", "养生",
        "养老", "家政", "服务", "中介", "代理", "经纪", "拍卖", "典当", "担保",
        "小贷", "创投", "私募", "公募", "资管", "投行", "券商"]

# 公司名后缀
COMPANY_SUFFIXES = ["有限公司", "有限责任公司", "股份有限公司", "集团公司",
        "控股公司", "总公司", "分公司", "子公司", "代表处", "办事处", "事务所",
        "工作室", "中心", "院", "所", "社"]

# 职位
JOB_TITLES = ["处长", "科长", "主任", "经理", "主管", "总监", "总裁", "董事长",
        "总经理", "副总经理", "秘书", "助理", "专员", "职员", "工程师", "技术员",
        "会计", "出纳", "律师", "顾问", "代表", "大使", "先生", "女士", "小姐",
        "夫人", "太太", "老板", "总", "董", "局", "处", "科", "厅", "部", "长",
        "书记", "委员", "理事", "监事", "高管", "中层", "基层", "员工", "工人",
        "师傅", "徒弟", "学长", "学弟", "师兄", "师弟", "同学", "同事", "同乡",
        "同胞", "同志", "朋友", "伙伴", "搭档", "手足", "兄弟", "哥们", "弟兄",
        "姊妹", "姐弟", "兄妹", "姐妹", "父女", "母子", "父子", "母女", "夫妻",
        "夫妇", "配偶", "爱人", "对象", "恋人", "情侣", "情人", "知己", "知音",
        "恩人", "贵人", "高人", "能人", "强人", "牛人", "达人", "专家", "学者",
        "教授", "讲师", "导师", "教练", "老师", "师父", "徒儿", "弟子", "学生",
        "学员", "听众", "观众", "读者", "用户", "客户", "顾客", "买家", "卖家",
        "商家", "厂家", "店家", "店主", "店员", "服务员", "营业员", "销售员",
        "推销员", "业务员", "经纪人", "代理人", "中间人", "介绍人", "担保人",
        "见证人", "证明人", "当事人", "关系人"]


def generate_chinese_name():
    """生成随机中文姓名"""
    surname = random.choice(SURNAMES)
    is_male = random.choice([True, False])
    given_names = GIVEN_NAMES_MALE if is_male else GIVEN_NAMES_FEMALE
    
    # 70% 单字名，30% 双字名
    if random.random() < 0.7:
        given_name = random.choice(given_names)
        return surname + given_name
    else:
        given_name1 = random.choice(given_names)
        given_name2 = random.choice(given_names)
        while given_name1 == given_name2:
            given_name2 = random.choice(given_names)
        return surname + given_name1 + given_name2


def generate_company_name():
    """生成随机公司名称"""
    prefix = random.choice(COMPANY_PREFIXES)
    industry = random.choice(COMPANY_INDUSTRIES)
    suffix = random.choice(COMPANY_SUFFIXES)
    
    # 30% 概率添加额外的修饰词
    if random.random() < 0.3:
        extra = random.choice(COMPANY_INDUSTRIES)
        return prefix + extra + industry + suffix
    
    return prefix + industry + suffix


def generate_address():
    """生成随机地址"""
    city = random.choice(CITIES)
    district = random.choice(DISTRICTS)
    street = random.choice(STREETS)
    building_num = f"{random.randint(1, 900)}号"
    building_type = random.choice(BUILDING_TYPES)
    
    # 50% 概率添加更详细的地址
    if random.random() < 0.5:
        room_num = random.randint(1000, 9999)
        return f"N'{city}{district}{street}{building_num}{building_type}{room_num}室'"
    
    return f"N'{city}{district}{street}{building_num}'"


def generate_telephone():
    """生成随机电话号码 (固定电话格式)"""
    # 统一使用 010-44 开头
    area_code = "010"
    phone_prefix = "44"
    # 生成剩余 6 位数字
    phone_suffix = ''.join([str(random.randint(0, 9)) for _ in range(6)])
    
    return f"N'{area_code}-{phone_prefix}{phone_suffix}'"


def generate_mobile_phone():
    """生成随机手机号码"""
    # 统一使用 121 开头
    prefix = "121"
    phone = prefix + ''.join([str(random.randint(0, 9)) for _ in range(8)])
    
    return f"N'{phone}'"


def generate_telephone_with_extension():
    """生成带分机号的电话号码"""
    area_code = "010"
    phone_prefix = "44"
    phone_suffix = ''.join([str(random.randint(0, 9)) for _ in range(6)])
    extension = ''.join([str(random.randint(0, 9)) for _ in range(3)])
    
    return f"N'{area_code}-{phone_prefix}{phone_suffix}-{extension}'"


def generate_short_phone():
    """生成短号码 (如 8589 1199 这种 8 位号码)"""
    prefix = "44"
    phone_suffix = ''.join([str(random.randint(0, 9)) for _ in range(6)])
    
    return f"N'{prefix}{phone_suffix}'"


def generate_email():
    """生成随机邮箱地址"""
    domains = ["qq.com", "163.com", "126.com", "sina.com", "sohu.com", "yahoo.com",
               "gmail.com", "hotmail.com", "outlook.com", "live.com", "foxmail.com",
               "yeah.net", "139.com", "wo.com.cn", "189.cn", "21cn.com", "tom.com",
               "china.com", "eyou.com", "51.com", "renren.com", "kaixin001.com",
               "douban.com", "alumni.com", "edu.cn", "mail.edu.cn", "students.edu.cn",
               "teachers.edu.cn", "prof.edu.cn", "research.edu.cn"]
    
    # 生成用户名部分
    if random.random() < 0.5:
        # 使用拼音风格的用户名
        name = generate_chinese_name()
        username = name.lower()
        # 如果太短或为空，使用随机字母数字
        if len(username) < 3:
            username = "user" + str(random.randint(0, 9999))
    else:
        # 随机字母 + 数字组合
        length = random.randint(4, 8)
        username = ''.join([chr(ord('a') + random.randint(0, 25)) if random.random() < 0.5 
                            else str(random.randint(0, 9)) for _ in range(length)])
    
    # 50% 概率添加数字后缀
    if random.random() < 0.5:
        username += str(random.randint(0, 9999))
    
    domain = random.choice(domains)
    return f"N'{username}@{domain}'"


def mask_sql_file(input_file, output_file):
    """替换 SQL 文件中的敏感数据"""
    with open(input_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    stats = {
        'company': 0,
        'name': 0,
        'phone': 0,
        'email': 0,
        'address': 0
    }
    
    # 替换公司名称 (包含公司、集团、单位等关键词的 N'xxx')
    def replace_company(match):
        stats['company'] += 1
        return generate_company_name()
    
    content = re.sub(r"N'([^']*(?:公司 | 集团 | 单位 | 学校 | 医院|中心 | 社 | 院|所|处|局|部|委|办|厅)[^']*)'", 
                    replace_company, content)
    
    # 替换人名 (2-4 个中文字符，避免替换地址)
    def replace_name(match):
        matched_text = match.group(1)
        # 避免替换地址中的中文
        if not any(x in matched_text for x in ["市", "区", "县", "路", "街", "镇", "乡", "村"]):
            stats['name'] += 1
            return "N'" + generate_chinese_name() + "'"
        return match.group(0)
    
    content = re.sub(r"N'([\u4e00-\u9fa5]{2,4})'", replace_name, content)
    
    # 替换固定电话号码 (包括多个号码连在一起的情况)
    def replace_telephone(match):
        stats['phone'] += 1
        return generate_telephone()
    
    content = re.sub(r"N'(\d{3,4}-\d{7,8})'", replace_telephone, content)
    
    # 处理多个电话号码连在一起的情况 (如：N'029-68812811 029-63391755 029-88866690 ')
    def replace_multiple_phones(match):
        nonlocal stats
        phones = match.group(1).strip().split()
        replaced_phones = []
        for phone in phones:
            if '-' in phone:
                stats['phone'] += 1
                replaced_phones.append(generate_telephone().replace("N'", "").replace("'", ""))
            else:
                replaced_phones.append(phone)
        return "N'" + ' '.join(replaced_phones) + "'"
    
    content = re.sub(r"N'((?:\d{3,4}-\d{7,8}\s*)+)'", replace_multiple_phones, content)
    
    # 处理带分机号的电话格式 (如：010-52786055-806)
    def replace_phone_with_extension(match):
        stats['phone'] += 1
        return generate_telephone_with_extension()
    
    content = re.sub(r"N'(\d{3,4}-\d+-\d+)'", replace_phone_with_extension, content)
    
    # 处理括号区号格式 (如：(010)85749006)
    def replace_bracket_phone(match):
        stats['phone'] += 1
        return generate_telephone()
    
    content = re.sub(r"N'\((\d{3,4})\)(\d{7,8})'", replace_bracket_phone, content)
    
    # 处理 8 位短号码 (如：8589 1199, 64243522) - 注意要排除已经替换过的
    def replace_short_phone(match):
        stats['phone'] += 1
        return generate_short_phone()
    
    content = re.sub(r"N'(\d{4}\s+\d{4})'", replace_short_phone, content)
    
    # 处理不带 N'的短号码 (如 '5128 4458', '6495 2113')
    def replace_short_phone_no_n(match):
        stats['phone'] += 1
        return generate_short_phone()
    
    content = re.sub(r"N'(\d{4}\s+\d{4})'", replace_short_phone_no_n, content)
    
    # 在 CRM_CUS 表中替换所有未处理的 8 位连续数字 (CRM_CUS_TELE 字段)
    lines = content.split('\n')
    new_lines = []
    in_crm_cus = False
    
    for line in lines:
        if 'INSERT INTO [CRM_CUS]' in line:
            in_crm_cus = True
        
        if in_crm_cus:
            import re as regex
            
            # 匹配模式：email 字段后的 8 位数字，且该数字不是 010-44 或 44 开头
            def replace_8digit_after_email(match):
                nonlocal stats
                phone = match.group(2)
                # 如果已经是处理过的格式，不替换
                if phone.startswith("010-") or phone.startswith("44"):
                    return match.group(0)
                
                stats['phone'] += 1
                short_phone = generate_short_phone().replace("N'", "").replace("'", "")
                return f"{match.group(1)}N'{short_phone}'{match.group(3)}"
            
            # 查找 CRM_CUS_TELE 字段中的 8 位数字
            # 模式：,N'email',N'8 位数字',N''
            line = regex.sub(r"(,N'[^']*?@[^']*?',)N'(\d{8})'(,)", replace_8digit_after_email, line)
        
        if ';' in line:
            in_crm_cus = False
            
        new_lines.append(line)
    
    content = '\n'.join(new_lines)

    # 替换手机号码
    def replace_mobile(match):
        stats['phone'] += 1
        return generate_mobile_phone()
    
    content = re.sub(r"N'(1[3-9]\d{9})'", replace_mobile, content)
    
    # 替换邮箱地址
    def replace_email(match):
        stats['email'] += 1
        return generate_email()
    
    content = re.sub(r"N'([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})'", 
                    replace_email, content)
    
    # 替换地址 (包含省、市、区、县、路、街、道等的 N'xxx' 字符串)
    def replace_address(match):
        stats['address'] += 1
        return generate_address()
    
    content = re.sub(r"N'([^']*(?:省 | 市 | 区 | 县 | 路 | 街 | 道 | 镇 | 乡 | 村 | 号 | 大厦 | 中心 | 广场 | 花园 | 小区)[^']*)'", 
                    replace_address, content)
    
    # 写入输出文件
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print("数据脱敏完成!")
    print("替换统计:")
    print(f"  - 公司名称：{stats['company']}")
    print(f"  - 人名：{stats['name']}")
    print(f"  - 电话：{stats['phone']}")
    print(f"  - 邮箱：{stats['email']}")
    print(f"  - 地址：{stats['address']}")


if __name__ == "__main__":
    import sys
    if len(sys.argv) < 3:
        print("用法：python3 sql_masker.py <input_file> <output_file>")
        print("示例：python3 sql_masker.py demo.sql demo_masked.sql")
        sys.exit(1)
    
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    
    try:
        mask_sql_file(input_file, output_file)
    except Exception as e:
        print(f"处理文件时出错：{e}")
        sys.exit(1)
