import { ref } from 'vue'
import type {
  VaccineBatch,
  DistributionOrder,
  TransitReport,
  AcceptanceRecord,
  TemperatureEvidenceGap,
  ReturnTask,
  ClinicInventory,
  QuantityConfirmation,
  DistributionRecommendation,
  SkippedBatchReview,
  BatchAdjustmentRecord,
  ColdChainBox,
  TransitSegment,
  BoxAcceptanceRecord,
} from '@/types'
import {
  mockBatches,
  mockDistributionOrders,
  mockTransitReports,
  mockAcceptanceRecords,
  mockEvidenceGaps,
  mockReturnTasks,
  mockClinicInventory,
  mockQuantityConfirmations,
} from '@/data/mock'

const API_BASE = '/api'

let backendAvailable: boolean | null = null

async function checkBackend(): Promise<boolean> {
  if (backendAvailable !== null) return backendAvailable
  try {
    const res = await fetch(API_BASE + '/batches', { method: 'HEAD' })
    backendAvailable = res.ok
  } catch {
    backendAvailable = false
  }
  return backendAvailable
}

function mapBatch(raw: any): VaccineBatch {
  return {
    id: String(raw.id),
    batchNo: raw.batchNo,
    vaccineName: raw.vaccineName,
    manufacturer: raw.manufacturer,
    specification: raw.specification,
    expiryDate: raw.expiryDate,
    storageTempZone: raw.storageTempZone,
    batchReleaseDoc: raw.batchReleaseDoc,
    distributableQty: raw.distributableQty,
    totalQty: raw.totalQty,
    status: raw.status?.toLowerCase() || 'available',
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

function mapOrder(raw: any): DistributionOrder {
  return {
    id: String(raw.id),
    orderNo: raw.orderNo,
    batchId: String(raw.batchId),
    batchNo: raw.batchNo,
    vaccineName: raw.vaccineName,
    quantity: raw.quantity,
    coldChainBoxNo: raw.coldChainBoxNo,
    tempProbeNo: raw.tempProbeNo,
    sealNo: raw.sealNo,
    vehicleNo: raw.vehicleNo,
    driverName: raw.driverName,
    driverPhone: raw.driverPhone,
    estimatedArrivalTime: raw.estimatedArrivalTime,
    targetClinic: raw.targetClinic,
    status: raw.status?.toLowerCase() || 'pending',
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

function mapTransitReport(raw: any): TransitReport {
  return {
    id: String(raw.id),
    distributionOrderId: String(raw.distributionOrderId),
    orderNo: raw.orderNo || '',
    reportTime: raw.reportTime,
    temperature: raw.temperature,
    latitude: raw.latitude,
    longitude: raw.longitude,
    locationDesc: raw.locationDesc,
    stopPoint: raw.stopPoint,
    boxOpenRecord: raw.boxOpenRecord,
    probeStatus: raw.probeStatus?.toLowerCase() || 'online',
    probeBatteryLevel: raw.probeBatteryLevel,
  }
}

function mapAcceptance(raw: any): AcceptanceRecord {
  return {
    id: String(raw.id),
    distributionOrderId: String(raw.distributionOrderId),
    orderNo: raw.orderNo,
    batchNo: raw.batchNo,
    vaccineName: raw.vaccineName,
    sentQty: raw.sentQty,
    receivedQty: raw.receivedQty,
    sealIntact: raw.sealIntact,
    tempCurveOk: raw.tempCurveOk,
    arrivalTime: raw.arrivalTime,
    status: raw.status?.toLowerCase() || 'pending',
    rejectionReason: raw.rejectionReason,
    returnTaskId: raw.returnTaskId ? String(raw.returnTaskId) : null,
    warehouseConfirmedQty: raw.warehouseConfirmedQty,
    clinicConfirmedQty: raw.clinicConfirmedQty,
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

function mapGap(raw: any): TemperatureEvidenceGap {
  return {
    id: String(raw.id),
    distributionOrderId: String(raw.distributionOrderId),
    orderNo: raw.orderNo,
    probeNo: raw.probeNo,
    offlineAt: raw.offlineAt,
    backOnlineAt: raw.backOnlineAt,
    duration: raw.duration,
    status: raw.status?.toLowerCase() || 'open',
    description: raw.description,
  }
}

function mapReturnTask(raw: any): ReturnTask {
  return {
    id: String(raw.id),
    acceptanceRecordId: String(raw.acceptanceRecordId),
    orderNo: raw.orderNo,
    reason: raw.reason,
    status: raw.status?.toLowerCase() || 'pending',
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

function mapInventory(raw: any): ClinicInventory {
  return {
    id: String(raw.id),
    batchId: String(raw.batchId),
    batchNo: raw.batchNo,
    vaccineName: raw.vaccineName,
    quantity: raw.quantity,
    storageTempZone: raw.storageTempZone,
    expiryDate: raw.expiryDate,
    clinicName: raw.clinicName,
    receivedAt: raw.receivedAt,
  }
}

function mapConfirmation(raw: any): QuantityConfirmation {
  return {
    id: String(raw.id),
    distributionOrderId: String(raw.distributionOrderId),
    orderNo: raw.orderNo,
    warehouseConfirmedQty: raw.warehouseConfirmedQty,
    clinicConfirmedQty: raw.clinicConfirmedQty,
    warehouseConfirmed: raw.warehouseConfirmed,
    clinicConfirmed: raw.clinicConfirmed,
    status: raw.status?.toLowerCase() || 'pending_both',
    createdAt: raw.createdAt,
  }
}

function mapRecommendation(raw: any): DistributionRecommendation {
  return {
    id: String(raw.id),
    batchId: String(raw.batchId),
    batchNo: raw.batchNo,
    vaccineName: raw.vaccineName,
    recommendedOrder: raw.recommendedOrder,
    expiryScore: raw.expiryScore,
    appointmentScore: raw.appointmentScore,
    inventoryScore: raw.inventoryScore,
    distanceScore: raw.distanceScore,
    capacityScore: raw.capacityScore,
    totalScore: raw.totalScore,
    daysToExpiry: raw.daysToExpiry,
    appointmentCount: raw.appointmentCount,
    targetClinic: raw.targetClinic,
    distanceKm: raw.distanceKm,
    coldChainBoxRemaining: raw.coldChainBoxRemaining,
    createdAt: raw.createdAt,
    createdBy: raw.createdBy,
  }
}

function mapSkippedReview(raw: any): SkippedBatchReview {
  return {
    id: String(raw.id),
    batchId: String(raw.batchId),
    batchNo: raw.batchNo,
    vaccineName: raw.vaccineName,
    skipReason: raw.skipReason,
    skippedBy: raw.skippedBy,
    skippedAt: raw.skippedAt,
    targetStatus: raw.targetStatus?.toLowerCase() || 'pending_arrangement',
    reviewComment: raw.reviewComment,
    reviewedBy: raw.reviewedBy,
    reviewedAt: raw.reviewedAt,
    reviewed: raw.reviewed,
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

function mapAdjustmentRecord(raw: any): BatchAdjustmentRecord {
  return {
    id: String(raw.id),
    batchId: String(raw.batchId),
    batchNo: raw.batchNo,
    recommendedOrder: raw.recommendedOrder,
    actualOrder: raw.actualOrder,
    skipReason: raw.skipReason,
    adjustedBy: raw.adjustedBy,
    affectedClinics: raw.affectedClinics,
    createdAt: raw.createdAt,
  }
}

function mapColdChainBox(raw: any): ColdChainBox {
  return {
    id: String(raw.id),
    boxNo: raw.boxNo,
    distributionOrderId: String(raw.distributionOrderId),
    orderNo: raw.orderNo,
    batchId: String(raw.batchId),
    batchNo: raw.batchNo,
    vaccineName: raw.vaccineName,
    quantity: raw.quantity,
    tempProbeNo: raw.tempProbeNo,
    sealNo: raw.sealNo,
    targetClinic: raw.targetClinic,
    currentVehicleNo: raw.currentVehicleNo,
    currentDriverName: raw.currentDriverName,
    currentDriverPhone: raw.currentDriverPhone,
    transferPoint: raw.transferPoint,
    estimatedArrivalTime: raw.estimatedArrivalTime,
    actualArrivalTime: raw.actualArrivalTime,
    status: raw.status?.toLowerCase() || 'pending',
    exceptionRemark: raw.exceptionRemark,
    responsibleParty: raw.responsibleParty,
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

function mapTransitSegment(raw: any): TransitSegment {
  return {
    id: String(raw.id),
    boxId: String(raw.boxId),
    boxNo: raw.boxNo,
    distributionOrderId: String(raw.distributionOrderId),
    orderNo: raw.orderNo,
    segmentOrder: raw.segmentOrder,
    fromPoint: raw.fromPoint,
    toPoint: raw.toPoint,
    vehicleNo: raw.vehicleNo,
    driverName: raw.driverName,
    driverPhone: raw.driverPhone,
    departTime: raw.departTime,
    estimatedArrivalTime: raw.estimatedArrivalTime,
    actualArrivalTime: raw.actualArrivalTime,
    status: raw.status?.toLowerCase() || 'pending',
    delayReason: raw.delayReason,
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

function mapBoxAcceptance(raw: any): BoxAcceptanceRecord {
  return {
    id: String(raw.id),
    boxId: String(raw.boxId),
    boxNo: raw.boxNo,
    distributionOrderId: String(raw.distributionOrderId),
    orderNo: raw.orderNo,
    batchNo: raw.batchNo,
    vaccineName: raw.vaccineName,
    sentQty: raw.sentQty,
    receivedQty: raw.receivedQty,
    sealIntact: raw.sealIntact,
    tempCurveOk: raw.tempCurveOk,
    arrivalTime: raw.arrivalTime,
    status: raw.status?.toLowerCase() || 'pending',
    rejectionReason: raw.rejectionReason,
    exceptionResponsibility: raw.exceptionResponsibility,
    returnTaskId: raw.returnTaskId ? String(raw.returnTaskId) : null,
    warehouseConfirmedQty: raw.warehouseConfirmedQty,
    clinicConfirmedQty: raw.clinicConfirmedQty,
    createdAt: raw.createdAt,
    updatedAt: raw.updatedAt,
  }
}

export function useApi() {
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchJson<T>(url: string): Promise<T> {
    loading.value = true
    error.value = null
    try {
      const res = await fetch(API_BASE + url)
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      return await res.json()
    } catch (e: any) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function postJson<T>(url: string, body: any): Promise<T> {
    loading.value = true
    error.value = null
    try {
      const res = await fetch(API_BASE + url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      return await res.json()
    } catch (e: any) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function putJson<T>(url: string, body: any): Promise<T> {
    loading.value = true
    error.value = null
    try {
      const res = await fetch(API_BASE + url, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      return await res.json()
    } catch (e: any) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function getBatches(): Promise<VaccineBatch[]> {
    const useReal = await checkBackend()
    if (!useReal) return mockBatches
    try {
      const raw = await fetchJson<any[]>('/batches')
      return raw.map(mapBatch)
    } catch {
      return mockBatches
    }
  }

  async function createBatch(batch: Omit<VaccineBatch, 'id' | 'createdAt' | 'updatedAt'>): Promise<VaccineBatch> {
    const useReal = await checkBackend()
    if (!useReal) {
      const newBatch: VaccineBatch = {
        ...batch,
        id: 'B' + String(mockBatches.length + 1).padStart(3, '0'),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      }
      mockBatches.push(newBatch)
      return newBatch
    }
    const raw = await postJson<any>('/batches', batch)
    return mapBatch(raw)
  }

  async function getDistributionOrders(): Promise<DistributionOrder[]> {
    const useReal = await checkBackend()
    if (!useReal) return mockDistributionOrders
    try {
      const raw = await fetchJson<any[]>('/distribution-orders')
      return raw.map(mapOrder)
    } catch {
      return mockDistributionOrders
    }
  }

  async function createDistributionOrder(order: Partial<DistributionOrder>): Promise<DistributionOrder> {
    const useReal = await checkBackend()
    if (!useReal) {
      const newOrder: DistributionOrder = {
        id: 'D' + String(mockDistributionOrders.length + 1).padStart(3, '0'),
        orderNo: order.orderNo || `DIS-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${String(mockDistributionOrders.length + 1).padStart(3, '0')}`,
        batchId: order.batchId || '',
        batchNo: order.batchNo || '',
        vaccineName: order.vaccineName || '',
        quantity: order.quantity || 0,
        coldChainBoxNo: order.coldChainBoxNo || '',
        tempProbeNo: order.tempProbeNo || '',
        sealNo: order.sealNo || '',
        vehicleNo: order.vehicleNo || '',
        driverName: order.driverName || '',
        driverPhone: order.driverPhone || '',
        estimatedArrivalTime: order.estimatedArrivalTime || '',
        targetClinic: order.targetClinic || '',
        status: 'pending',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      }
      mockDistributionOrders.push(newOrder)
      return newOrder
    }
    const raw = await postJson<any>('/distribution-orders', order)
    return mapOrder(raw)
  }

  async function getTransitReports(orderId?: string): Promise<TransitReport[]> {
    const useReal = await checkBackend()
    if (!useReal) {
      if (orderId) return mockTransitReports.filter(r => r.distributionOrderId === orderId)
      return mockTransitReports
    }
    try {
      const url = orderId ? `/transit-reports?orderId=${orderId}` : '/transit-reports'
      const raw = await fetchJson<any[]>(url)
      return raw.map(mapTransitReport)
    } catch {
      return mockTransitReports
    }
  }

  async function getAcceptanceRecords(): Promise<AcceptanceRecord[]> {
    const useReal = await checkBackend()
    if (!useReal) return mockAcceptanceRecords
    try {
      const raw = await fetchJson<any[]>('/acceptance-records')
      return raw.map(mapAcceptance)
    } catch {
      return mockAcceptanceRecords
    }
  }

  async function acceptDelivery(recordId: string, receivedQty: number): Promise<AcceptanceRecord> {
    const useReal = await checkBackend()
    if (!useReal) {
      const record = mockAcceptanceRecords.find(r => r.id === recordId)
      if (record) {
        record.status = 'accepted'
        record.receivedQty = receivedQty
        record.updatedAt = new Date().toISOString()
      }
      return record!
    }
    const raw = await putJson<any>(`/acceptance-records/${recordId}/accept`, { receivedQty })
    return mapAcceptance(raw)
  }

  async function rejectDelivery(recordId: string, reason: string): Promise<AcceptanceRecord> {
    const useReal = await checkBackend()
    if (!useReal) {
      const record = mockAcceptanceRecords.find(r => r.id === recordId)
      if (record) {
        record.status = 'rejected'
        record.rejectionReason = reason
        record.updatedAt = new Date().toISOString()
        const returnTask: ReturnTask = {
          id: 'RT' + String(mockReturnTasks.length + 1).padStart(3, '0'),
          acceptanceRecordId: recordId,
          orderNo: record.orderNo,
          reason,
          status: 'pending',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        }
        record.returnTaskId = returnTask.id
        mockReturnTasks.push(returnTask)
      }
      return record!
    }
    const raw = await putJson<any>(`/acceptance-records/${recordId}/reject`, { reason })
    return mapAcceptance(raw)
  }

  async function confirmQuantity(recordId: string, side: 'warehouse' | 'clinic', qty: number): Promise<QuantityConfirmation> {
    const useReal = await checkBackend()
    if (!useReal) {
      let qc = mockQuantityConfirmations.find(q => q.distributionOrderId === mockAcceptanceRecords.find(r => r.id === recordId)?.distributionOrderId)
      if (!qc) {
        const record = mockAcceptanceRecords.find(r => r.id === recordId)
        qc = {
          id: 'QC' + String(mockQuantityConfirmations.length + 1).padStart(3, '0'),
          distributionOrderId: record?.distributionOrderId || '',
          orderNo: record?.orderNo || '',
          warehouseConfirmedQty: null,
          clinicConfirmedQty: null,
          warehouseConfirmed: false,
          clinicConfirmed: false,
          status: 'pending_both',
          createdAt: new Date().toISOString(),
        }
        mockQuantityConfirmations.push(qc)
      }
      if (side === 'warehouse') {
        qc.warehouseConfirmedQty = qty
        qc.warehouseConfirmed = true
      } else {
        qc.clinicConfirmedQty = qty
        qc.clinicConfirmed = true
      }
      if (qc.warehouseConfirmed && qc.clinicConfirmed) {
        qc.status = 'confirmed'
      } else if (qc.warehouseConfirmed) {
        qc.status = 'pending_clinic'
      } else {
        qc.status = 'pending_warehouse'
      }
      return qc
    }
    const raw = await postJson<any>(`/acceptance-records/${recordId}/confirm-quantity`, { side, qty })
    return mapConfirmation(raw)
  }

  async function getEvidenceGaps(): Promise<TemperatureEvidenceGap[]> {
    const useReal = await checkBackend()
    if (!useReal) return mockEvidenceGaps
    try {
      const raw = await fetchJson<any[]>('/evidence-gaps')
      return raw.map(mapGap)
    } catch {
      return mockEvidenceGaps
    }
  }

  async function submitTransitReport(report: {
    distributionOrderId: string
    temperature: number
    latitude: number
    longitude: number
    locationDesc: string
    stopPoint: string | null
    boxOpenRecord: string | null
    probeStatus: string
    probeBatteryLevel: number
  }): Promise<TransitReport> {
    const useReal = await checkBackend()
    if (!useReal) {
      const newReport: TransitReport = {
        id: 'TR' + String(mockTransitReports.length + 1).padStart(3, '0'),
        distributionOrderId: report.distributionOrderId,
        orderNo: '',
        reportTime: new Date().toISOString(),
        temperature: report.temperature,
        latitude: report.latitude,
        longitude: report.longitude,
        locationDesc: report.locationDesc,
        stopPoint: report.stopPoint,
        boxOpenRecord: report.boxOpenRecord,
        probeStatus: report.probeStatus as TransitReport['probeStatus'],
        probeBatteryLevel: report.probeBatteryLevel,
      }
      if (report.probeStatus === 'offline') {
        const order = mockDistributionOrders.find(o => o.id === report.distributionOrderId)
        const newGap: TemperatureEvidenceGap = {
          id: 'G' + String(mockEvidenceGaps.length + 1).padStart(3, '0'),
          distributionOrderId: report.distributionOrderId,
          orderNo: order?.orderNo || '',
          probeNo: order?.tempProbeNo || '',
          offlineAt: new Date().toISOString(),
          backOnlineAt: null,
          duration: null,
          status: 'open',
          description: '探头' + (order?.tempProbeNo || '') + '离线，温度数据缺失',
        }
        mockEvidenceGaps.push(newGap)
      }
      mockTransitReports.push(newReport)
      return newReport
    }
    const raw = await postJson<any>('/transit-reports', {
      distributionOrderId: Number(report.distributionOrderId),
      temperature: report.temperature,
      latitude: report.latitude,
      longitude: report.longitude,
      locationDesc: report.locationDesc,
      stopPoint: report.stopPoint,
      boxOpenRecord: report.boxOpenRecord,
      probeStatus: report.probeStatus.toUpperCase(),
      probeBatteryLevel: report.probeBatteryLevel,
    })
    return mapTransitReport(raw)
  }

  async function acknowledgeGap(recordId: string): Promise<AcceptanceRecord> {
    const useReal = await checkBackend()
    if (!useReal) {
      const record = mockAcceptanceRecords.find(r => r.id === recordId)
      if (record) {
        record.status = 'pending'
        record.updatedAt = new Date().toISOString()
        mockEvidenceGaps
          .filter(g => g.distributionOrderId === record.distributionOrderId && g.status !== 'acknowledged')
          .forEach(g => { g.status = 'acknowledged' })
      }
      return record!
    }
    const raw = await putJson<any>(`/acceptance-records/${recordId}/acknowledge-gap`, {})
    return mapAcceptance(raw)
  }

  async function resumeInbound(recordId: string): Promise<AcceptanceRecord> {
    const useReal = await checkBackend()
    if (!useReal) {
      const record = mockAcceptanceRecords.find(r => r.id === recordId)
      if (record) {
        record.tempCurveOk = true
        record.status = 'pending'
        record.updatedAt = new Date().toISOString()
      }
      return record!
    }
    const raw = await putJson<any>(`/acceptance-records/${recordId}/resume-inbound`, {})
    return mapAcceptance(raw)
  }

  async function getReturnTasks(): Promise<ReturnTask[]> {
    const useReal = await checkBackend()
    if (!useReal) return mockReturnTasks
    try {
      const raw = await fetchJson<any[]>('/return-tasks')
      return raw.map(mapReturnTask)
    } catch {
      return mockReturnTasks
    }
  }

  async function getClinicInventory(): Promise<ClinicInventory[]> {
    const useReal = await checkBackend()
    if (!useReal) return mockClinicInventory
    try {
      const raw = await fetchJson<any[]>('/clinic-inventory')
      return raw.map(mapInventory)
    } catch {
      return mockClinicInventory
    }
  }

  async function getDistributionRecommendations(vaccineName?: string): Promise<DistributionRecommendation[]> {
    const useReal = await checkBackend()
    if (!useReal) return []
    try {
      const url = vaccineName ? `/distribution-recommendations?vaccineName=${encodeURIComponent(vaccineName)}` : '/distribution-recommendations'
      const raw = await fetchJson<any[]>(url)
      return raw.map(mapRecommendation)
    } catch {
      return []
    }
  }

  async function generateDistributionRecommendations(params: {
    vaccineName?: string
    targetClinic?: string
    distanceKm?: number
    coldChainBoxCapacity?: number
    coldChainBoxUsed?: number
    batchIds?: string[]
    operator?: string
  }): Promise<DistributionRecommendation[]> {
    const useReal = await checkBackend()
    if (!useReal) return []
    try {
      const raw = await postJson<any[]>('/distribution-recommendations/generate', params)
      return raw.map(mapRecommendation)
    } catch {
      return []
    }
  }

  async function getPendingSkippedReviews(): Promise<SkippedBatchReview[]> {
    const useReal = await checkBackend()
    if (!useReal) return []
    try {
      const raw = await fetchJson<any[]>('/skipped-batch-reviews/pending')
      return raw.map(mapSkippedReview)
    } catch {
      return []
    }
  }

  async function skipNearExpiryBatch(params: {
    skippedBatchId: string
    selectedBatchId: string
    skipReason: string
    targetStatus: string
    skippedBy: string
    affectedClinics?: string
  }): Promise<SkippedBatchReview> {
    const useReal = await checkBackend()
    const raw = await postJson<any>('/skipped-batch-reviews', params)
    return mapSkippedReview(raw)
  }

  async function reviewSkippedBatch(reviewId: string, params: {
    reviewComment: string
    reviewedBy: string
    approved: boolean
  }): Promise<SkippedBatchReview> {
    const raw = await putJson<any>(`/skipped-batch-reviews/${reviewId}/review`, params)
    return mapSkippedReview(raw)
  }

  async function getBatchAdjustmentRecords(batchId: string): Promise<BatchAdjustmentRecord[]> {
    const useReal = await checkBackend()
    if (!useReal) return []
    try {
      const raw = await fetchJson<any[]>(`/batch-adjustment-records/batch/${batchId}`)
      return raw.map(mapAdjustmentRecord)
    } catch {
      return []
    }
  }

  async function updateBatchStatus(batchId: string, params: {
    status: string
    operator: string
    remark?: string
  }): Promise<VaccineBatch> {
    const raw = await putJson<any>(`/batches/${batchId}/status`, params)
    return mapBatch(raw)
  }

  async function getColdChainBoxes(batchId?: string, orderId?: string): Promise<ColdChainBox[]> {
    const useReal = await checkBackend()
    if (!useReal) return []
    try {
      let url = '/cold-chain-boxes'
      const params = new URLSearchParams()
      if (batchId) params.append('batchId', batchId)
      if (orderId) params.append('orderId', orderId)
      if (params.toString()) url += '?' + params.toString()
      const raw = await fetchJson<any[]>(url)
      return raw.map(mapColdChainBox)
    } catch {
      return []
    }
  }

  async function getColdChainBox(id: string): Promise<ColdChainBox> {
    const raw = await fetchJson<any>(`/cold-chain-boxes/${id}`)
    return mapColdChainBox(raw)
  }

  async function getBoxSegments(boxId: string): Promise<TransitSegment[]> {
    const raw = await fetchJson<any[]>(`/cold-chain-boxes/${boxId}/segments`)
    return raw.map(mapTransitSegment)
  }

  async function getBoxAcceptance(boxId: string): Promise<BoxAcceptanceRecord> {
    const raw = await fetchJson<any>(`/cold-chain-boxes/${boxId}/acceptance`)
    return mapBoxAcceptance(raw)
  }

  async function splitBatch(request: {
    batchId: string
    boxes: Array<{
      quantity: number
      coldChainBoxNo: string
      tempProbeNo: string
      sealNo: string
      vehicleNo: string
      driverName: string
      driverPhone: string
      targetClinic: string
      estimatedArrivalTime?: string
      transferPoint?: string
    }>
  }): Promise<ColdChainBox[]> {
    const raw = await postJson<any[]>('/cold-chain-boxes/split', {
      batchId: Number(request.batchId),
      boxes: request.boxes,
    })
    return raw.map(mapColdChainBox)
  }

  async function transferBox(request: {
    boxId: string
    fromPoint: string
    toPoint: string
    newVehicleNo: string
    newDriverName: string
    newDriverPhone: string
    transferRemark?: string
  }): Promise<TransitSegment> {
    const raw = await postJson<any>('/cold-chain-boxes/transfer', {
      boxId: Number(request.boxId),
      fromPoint: request.fromPoint,
      toPoint: request.toPoint,
      newVehicleNo: request.newVehicleNo,
      newDriverName: request.newDriverName,
      newDriverPhone: request.newDriverPhone,
      transferRemark: request.transferRemark,
    })
    return mapTransitSegment(raw)
  }

  async function acceptBox(boxId: string, request: {
    receivedQty: number
    sealIntact: boolean
    tempCurveOk: boolean
    exceptionResponsibility?: string
  }): Promise<BoxAcceptanceRecord> {
    const raw = await postJson<any>(`/cold-chain-boxes/${boxId}/accept`, request)
    return mapBoxAcceptance(raw)
  }

  async function rejectBox(boxId: string, request: {
    rejectionReason: string
    exceptionResponsibility?: string
  }): Promise<BoxAcceptanceRecord> {
    const raw = await postJson<any>(`/cold-chain-boxes/${boxId}/reject`, request)
    return mapBoxAcceptance(raw)
  }

  async function updateBoxStatus(boxId: string, request: {
    status: string
    remark?: string
    responsibleParty?: string
  }): Promise<ColdChainBox> {
    const raw = await putJson<any>(`/cold-chain-boxes/${boxId}/status`, request)
    return mapColdChainBox(raw)
  }

  async function markVehicleDelayed(vehicleNo: string, delayReason: string): Promise<void> {
    await postJson<any>('/cold-chain-boxes/mark-vehicle-delayed', { vehicleNo, delayReason })
  }

  async function getDelayedBoxes(): Promise<ColdChainBox[]> {
    const raw = await fetchJson<any[]>('/cold-chain-boxes/delayed')
    return raw.map(mapColdChainBox)
  }

  return {
    loading,
    error,
    getBatches,
    createBatch,
    getDistributionOrders,
    createDistributionOrder,
    getTransitReports,
    submitTransitReport,
    getAcceptanceRecords,
    acceptDelivery,
    rejectDelivery,
    confirmQuantity,
    acknowledgeGap,
    resumeInbound,
    getEvidenceGaps,
    getReturnTasks,
    getClinicInventory,
    getDistributionRecommendations,
    generateDistributionRecommendations,
    getPendingSkippedReviews,
    skipNearExpiryBatch,
    reviewSkippedBatch,
    getBatchAdjustmentRecords,
    updateBatchStatus,
    getColdChainBoxes,
    getColdChainBox,
    getBoxSegments,
    getBoxAcceptance,
    splitBatch,
    transferBox,
    acceptBox,
    rejectBox,
    updateBoxStatus,
    markVehicleDelayed,
    getDelayedBoxes,
  }
}
