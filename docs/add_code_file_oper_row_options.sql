-- CODE_FILE_OPER：行级失败策略与跳过表头（MySQL）
-- stop_on_row_error：1 遇行级错误立即中断（默认，兼容原行为）；0 允许失败继续
--   - download_deal_type=2：逐行删插失败则跳过该行
--   - download_deal_type=1/3：整批插入失败则降级为逐行插入，仅失败行跳过
-- max_row_errors：stop_on_row_error=0 时最多允许的失败行数，超出则中断；NULL 表示不限制
-- skip_header_lines：入库前跳过文件前 N 行（表头等），NULL/0 表示不跳过

ALTER TABLE code_file_oper ADD COLUMN stop_on_row_error INT DEFAULT 1 COMMENT '行级入库失败是否中断：1是 0否（2逐行跳过；1/3批量失败则降级逐行）；NULL视为1';
ALTER TABLE code_file_oper ADD COLUMN max_row_errors INT COMMENT '非中断模式下允许的最大失败行数，超出则中断；NULL不限制';
ALTER TABLE code_file_oper ADD COLUMN skip_header_lines INT DEFAULT 0 COMMENT '跳过文件前N行后再解析入库';
