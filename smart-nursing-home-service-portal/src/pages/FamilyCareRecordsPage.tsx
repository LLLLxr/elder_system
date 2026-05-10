import { Button, DatePicker, Space, Table, Tabs } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import FamilyPageScaffold from '../components/FamilyPageScaffold';
import CareRecordsDetailTabs from '../components/CareRecordsDetailTabs';
import { useActiveElderGuard } from '../components/useActiveElderGuard';
import { useCareRecords } from '../hooks/useCareRecords';
import { formatDateTime } from '../utils/dateFormat';
import { summarizeDoctorContent, summarizeNurseRecord } from '../utils/careRecordUtils';
import type { DoctorRoundRecord, NurseCareRecord } from '../types/care';

export default function FamilyCareRecordsPage() {
  const { activeElderId, hasActiveElder, guardMessage } = useActiveElderGuard();

  const {
    loading,
    errorMessage,
    recordDate,
    setRecordDate,
    nurseRecords,
    doctorRecords,
    selectedNurseRecord,
    selectedDoctorRecord,
    setSelectedNurseRecord,
    setSelectedDoctorRecord,
    loadRecords,
  } = useCareRecords(activeElderId, guardMessage);

  const nurseColumns: ColumnsType<NurseCareRecord> = [
    { title: '记录ID', dataIndex: 'recordId', key: 'recordId', width: 100 },
    { title: '护士', dataIndex: 'nurseName', key: 'nurseName', width: 140, render: (value?: string) => value || '-' },
    { title: '记录日期', dataIndex: 'recordDate', key: 'recordDate', width: 140, render: (value?: string) => value || '-' },
    {
      title: '护理摘要',
      key: 'summary',
      width: 180,
      render: (_value, record) => summarizeNurseRecord(record),
    },
  ];

  const doctorColumns: ColumnsType<DoctorRoundRecord> = [
    { title: '记录ID', dataIndex: 'roundRecordId', key: 'roundRecordId', width: 100 },
    { title: '医生', dataIndex: 'doctorName', key: 'doctorName', width: 140, render: (value?: string) => value || '-' },
    { title: '查房时间', dataIndex: 'roundTime', key: 'roundTime', width: 200, render: (value?: string) => formatDateTime(value) },
    {
      title: '查房摘要',
      dataIndex: 'content',
      key: 'contentSummary',
      width: 220,
      render: (value?: string) => summarizeDoctorContent(value),
    },
  ];

  return (
    <FamilyPageScaffold
      title="护理与查房记录"
      actions={
        <Space>
          <DatePicker value={recordDate} onChange={setRecordDate} />
          <Button onClick={() => void loadRecords(recordDate?.format('YYYY-MM-DD'))} disabled={!hasActiveElder}>
            查询
          </Button>
        </Space>
      }
      infoMessage="家属可查看护士护理记录与医生查房记录，列表将默认展示首条记录详情。"
      errorMessage={errorMessage}
      listTitle="护理与查房列表"
      listContent={
        <Tabs
          items={[
            {
              key: 'nurse',
              label: '护理记录',
              children: (
                <Table<NurseCareRecord>
                  rowKey={(record) => String(record.recordId)}
                  loading={loading}
                  columns={nurseColumns}
                  dataSource={nurseRecords}
                  pagination={{ pageSize: 6 }}
                  onRow={(record) => ({ onClick: () => setSelectedNurseRecord(record) })}
                  locale={{ emptyText: loading ? '正在加载护理记录...' : hasActiveElder ? '暂无护理记录' : guardMessage }}
                  rowClassName={(record) =>
                    record.recordId === selectedNurseRecord?.recordId ? 'ant-table-row-selected' : ''
                  }
                />
              ),
            },
            {
              key: 'doctor',
              label: '查房记录',
              children: (
                <Table<DoctorRoundRecord>
                  rowKey={(record) => String(record.roundRecordId)}
                  loading={loading}
                  columns={doctorColumns}
                  dataSource={doctorRecords}
                  pagination={{ pageSize: 6 }}
                  onRow={(record) => ({ onClick: () => setSelectedDoctorRecord(record) })}
                  locale={{ emptyText: loading ? '正在加载查房记录...' : hasActiveElder ? '暂无查房记录' : guardMessage }}
                  rowClassName={(record) =>
                    record.roundRecordId === selectedDoctorRecord?.roundRecordId ? 'ant-table-row-selected' : ''
                  }
                />
              ),
            },
          ]}
        />
      }
      detailTitle="记录详情"
      detailContent={
        <CareRecordsDetailTabs
          selectedNurseRecord={selectedNurseRecord}
          selectedDoctorRecord={selectedDoctorRecord}
          hasActiveElder={hasActiveElder}
          guardMessage={guardMessage}
        />
      }
    />
  );
}
