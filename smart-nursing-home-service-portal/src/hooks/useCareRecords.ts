import { useEffect, useState } from 'react';
import type { Dayjs } from 'dayjs';
import { listFamilyDoctorRoundRecords } from '../api/healthApi';
import { listFamilyNurseCareRecords } from '../api/careDeliveryApi';
import { extractApiErrorMessage } from '../api/client';
import type { DoctorRoundRecord, NurseCareRecord } from '../types/care';

export function useCareRecords(activeElderId?: number, guardMessage?: string) {
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [recordDate, setRecordDate] = useState<Dayjs | null>(null);
  const [nurseRecords, setNurseRecords] = useState<NurseCareRecord[]>([]);
  const [doctorRecords, setDoctorRecords] = useState<DoctorRoundRecord[]>([]);
  const [selectedNurseRecord, setSelectedNurseRecord] = useState<NurseCareRecord | null>(null);
  const [selectedDoctorRecord, setSelectedDoctorRecord] = useState<DoctorRoundRecord | null>(null);

  const loadRecords = async (dateText?: string) => {
    if (!activeElderId) {
      setNurseRecords([]);
      setDoctorRecords([]);
      setSelectedNurseRecord(null);
      setSelectedDoctorRecord(null);
      setErrorMessage(guardMessage ?? null);
      return;
    }

    setLoading(true);
    setErrorMessage(null);
    try {
      const [nurseData, doctorData] = await Promise.all([
        listFamilyNurseCareRecords(activeElderId, dateText),
        listFamilyDoctorRoundRecords(activeElderId, dateText),
      ]);
      const currentNurseRecordId = selectedNurseRecord?.recordId;
      const currentDoctorRecordId = selectedDoctorRecord?.roundRecordId;
      setNurseRecords(nurseData);
      setDoctorRecords(doctorData);
      setSelectedNurseRecord(nurseData.find((item) => item.recordId === currentNurseRecordId) ?? nurseData[0] ?? null);
      setSelectedDoctorRecord(
        doctorData.find((item) => item.roundRecordId === currentDoctorRecordId) ?? doctorData[0] ?? null,
      );
    } catch (error) {
      setNurseRecords([]);
      setDoctorRecords([]);
      setSelectedNurseRecord(null);
      setSelectedDoctorRecord(null);
      setErrorMessage(extractApiErrorMessage(error, '加载护理/查房记录失败'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadRecords();
  }, [activeElderId]);

  return {
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
  };
}
