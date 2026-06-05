export interface VaccineBatch {
  id: string
  batchNo: string
  vaccineName: string
  manufacturer: string
  specification: string
  expiryDate: string
  storageTempZone: string
  batchReleaseDoc: string
  distributableQty: number
  totalQty: number
  status: 'available' | 'partial' | 'exhausted' | 'expired' | 'pending_arrangement' | 'pending_report_loss' | 'pending_transfer' | 'under_review'
  recommendedOrder?: number
  lastAdjustedBy?: string
  lastAdjustedAt?: string
  affectedClinics?: string
  createdAt: string
  updatedAt: string
}

export interface DistributionOrder {
  id: string
  orderNo: string
  batchId: string
  batchNo: string
  vaccineName: string
  quantity: number
  coldChainBoxNo: string
  tempProbeNo: string
  sealNo: string
  vehicleNo: string
  driverName: string
  driverPhone: string
  estimatedArrivalTime: string
  targetClinic: string
  status: 'pending' | 'outbound' | 'in_transit' | 'arrived' | 'accepted' | 'rejected' | 'returned'
  createdAt: string
  updatedAt: string
}

export interface TransitReport {
  id: string
  distributionOrderId: string
  orderNo: string
  reportTime: string
  temperature: number
  latitude: number
  longitude: number
  locationDesc: string
  stopPoint: string | null
  boxOpenRecord: string | null
  probeStatus: 'online' | 'offline' | 'low_battery'
  probeBatteryLevel: number
}

export interface TemperatureRecord {
  time: string
  temperature: number
}

export interface AcceptanceRecord {
  id: string
  distributionOrderId: string
  orderNo: string
  batchNo: string
  vaccineName: string
  sentQty: number
  receivedQty: number
  sealIntact: boolean
  tempCurveOk: boolean
  arrivalTime: string
  status: 'pending' | 'accepted' | 'rejected' | 'qty_mismatch' | 'temp_gap' | 'evidence_gap'
  rejectionReason: string | null
  returnTaskId: string | null
  warehouseConfirmedQty: number | null
  clinicConfirmedQty: number | null
  createdAt: string
  updatedAt: string
}

export interface TemperatureEvidenceGap {
  id: string
  distributionOrderId: string
  orderNo: string
  probeNo: string
  offlineAt: string
  backOnlineAt: string | null
  duration: number | null
  status: 'open' | 'closed' | 'acknowledged'
  description: string
}

export interface ReturnTask {
  id: string
  acceptanceRecordId: string
  orderNo: string
  reason: string
  status: 'pending' | 'in_return' | 'returned' | 'disposed'
  createdAt: string
  updatedAt: string
}

export interface ClinicInventory {
  id: string
  batchId: string
  batchNo: string
  vaccineName: string
  quantity: number
  storageTempZone: string
  expiryDate: string
  clinicName: string
  receivedAt: string
}

export interface QuantityConfirmation {
  id: string
  distributionOrderId: string
  orderNo: string
  warehouseConfirmedQty: number | null
  clinicConfirmedQty: number | null
  warehouseConfirmed: boolean
  clinicConfirmed: boolean
  status: 'pending_warehouse' | 'pending_clinic' | 'pending_both' | 'confirmed'
  createdAt: string
}

export type TempZone = '2-8°C' | '-20°C' | '-70°C' | '常温'

export const TEMP_ZONE_OPTIONS: TempZone[] = ['2-8°C', '-20°C', '-70°C', '常温']

export const BATCH_STATUS_MAP: Record<VaccineBatch['status'], { label: string; color: string }> = {
  available: { label: '可配送', color: 'green' },
  partial: { label: '部分配送', color: 'yellow' },
  exhausted: { label: '已配完', color: 'gray' },
  expired: { label: '已过期', color: 'red' },
}

export const ORDER_STATUS_MAP: Record<DistributionOrder['status'], { label: string; color: string }> = {
  pending: { label: '待出库', color: 'gray' },
  outbound: { label: '已出库', color: 'blue' },
  in_transit: { label: '在途', color: 'yellow' },
  arrived: { label: '已到达', color: 'purple' },
  accepted: { label: '已验收', color: 'green' },
  rejected: { label: '已拒收', color: 'red' },
  returned: { label: '已回库', color: 'orange' },
}

export const ACCEPTANCE_STATUS_MAP: Record<AcceptanceRecord['status'], { label: string; color: string }> = {
  pending: { label: '待验收', color: 'gray' },
  accepted: { label: '验收通过', color: 'green' },
  rejected: { label: '已拒收', color: 'red' },
  qty_mismatch: { label: '数量不符', color: 'orange' },
  temp_gap: { label: '温度异常', color: 'yellow' },
  evidence_gap: { label: '证据缺口', color: 'purple' },
}

export const PROBE_STATUS_MAP: Record<TransitReport['probeStatus'], { label: string; color: string }> = {
  online: { label: '在线', color: 'green' },
  offline: { label: '离线', color: 'red' },
  low_battery: { label: '低电量', color: 'yellow' },
}

export const EVIDENCE_GAP_STATUS_MAP: Record<TemperatureEvidenceGap['status'], { label: string; color: string }> = {
  open: { label: '未关闭', color: 'red' },
  closed: { label: '已关闭', color: 'green' },
  acknowledged: { label: '已确认', color: 'yellow' },
}

export const RETURN_TASK_STATUS_MAP: Record<ReturnTask['status'], { label: string; color: string }> = {
  pending: { label: '待回库', color: 'gray' },
  in_return: { label: '回库中', color: 'blue' },
  returned: { label: '已回库', color: 'green' },
  disposed: { label: '已销毁', color: 'red' },
}

export interface DistributionRecommendation {
  id: string
  batchId: string
  batchNo: string
  vaccineName: string
  recommendedOrder: number
  expiryScore: number
  appointmentScore: number
  inventoryScore: number
  distanceScore: number
  capacityScore: number
  totalScore: number
  daysToExpiry: number
  appointmentCount: number
  targetClinic?: string
  distanceKm?: number
  coldChainBoxRemaining?: number
  createdAt: string
  createdBy?: string
}

export interface SkippedBatchReview {
  id: string
  batchId: string
  batchNo: string
  vaccineName: string
  skipReason: string
  skippedBy: string
  skippedAt: string
  targetStatus: string
  reviewComment?: string
  reviewedBy?: string
  reviewedAt?: string
  reviewed: boolean
  createdAt: string
  updatedAt?: string
}

export interface BatchAdjustmentRecord {
  id: string
  batchId: string
  batchNo: string
  recommendedOrder: number
  actualOrder: number
  skipReason?: string
  adjustedBy?: string
  affectedClinics?: string
  createdAt: string
}

export const BATCH_STATUS_MAP: Record<VaccineBatch['status'], { label: string; color: string }> = {
  available: { label: '可配送', color: 'green' },
  partial: { label: '部分配送', color: 'yellow' },
  exhausted: { label: '已配完', color: 'gray' },
  expired: { label: '已过期', color: 'red' },
  pending_arrangement: { label: '待安排', color: 'orange' },
  pending_report_loss: { label: '待报损', color: 'red' },
  pending_transfer: { label: '待转配', color: 'purple' },
  under_review: { label: '复核中', color: 'blue' },
}
