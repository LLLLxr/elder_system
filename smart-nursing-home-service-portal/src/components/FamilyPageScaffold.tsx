import type { ReactNode } from 'react';
import { Alert } from 'antd';
import DefaultCollapsedSection from './DefaultCollapsedSection';
import JourneyPageScaffold from './JourneyPageScaffold';

type FamilyPageScaffoldProps = {
  title: string;
  actions?: ReactNode;
  infoMessage?: ReactNode;
  errorMessage?: ReactNode;
  listTitle: ReactNode;
  listContent: ReactNode;
  detailTitle?: ReactNode;
  detailContent?: ReactNode;
};

export default function FamilyPageScaffold({
  title,
  actions,
  infoMessage,
  errorMessage,
  listTitle,
  listContent,
  detailTitle,
  detailContent,
}: FamilyPageScaffoldProps) {
  return (
    <JourneyPageScaffold title={title} actions={actions}>
      {infoMessage ? <Alert type="info" showIcon message={infoMessage} /> : null}
      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}

      <DefaultCollapsedSection title={listTitle}>{listContent}</DefaultCollapsedSection>
      {detailTitle && detailContent ? (
        <DefaultCollapsedSection title={detailTitle}>{detailContent}</DefaultCollapsedSection>
      ) : null}
    </JourneyPageScaffold>
  );
}
