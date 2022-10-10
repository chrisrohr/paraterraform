<template>
  <div class="q-pa-md">
    <div class="row flex flex-center">
      <h3>History Page</h3>
    </div>

    <div class="row flex flex-center">
      <q-table
        :rows="latestStates"
        :columns="columns"
        :expanded="expanded"
        row-key="name"
        style="width: 70%"
      >
        <template v-slot:body="props">
          <q-tr :props="props">
            <q-td>
              <q-btn
                     flat
                     dense
                     @click="getHistoryForNameExpand(props)"
                     :icon="props.expand ? 'expand_less' : 'expand_more'" />
            </q-td>
            <q-td key="name" :props="props">{{ props.row.name }}</q-td>
            <q-td key="name" :props="props">
              <span>
                {{ this.formatDateRelative(props.row.uploadedAt) }}
                <q-tooltip>
                  {{ this.formatDateISO(props.row.uploadedAt) }}
                </q-tooltip>
              </span>
            </q-td>
            <q-td key="name" :props="props"></q-td>
          </q-tr>
          <q-tr v-show="props.expand"
                v-for="fileState in this.historyForSpecificState"
                :key="fileState.id"
          style="background-color: #dbdbd9">
            <q-td></q-td>
            <q-td key="name" :props="props"> {{ fileState.name }}</q-td>
            <q-td key="uploadedAt" :props="props">
              <span>
                {{ this.formatDateRelative(fileState.uploadedAt) }}
                <q-tooltip>
                  {{ this.formatDateISO(fileState.uploadedAt) }}
                </q-tooltip>
              </span>
            </q-td>
            <q-td>
              <q-btn icon="delete" flat dense @click="deleteState(fileState)"></q-btn>
            </q-td>
          </q-tr>
        </template>
      </q-table>
    </div>
  </div>
</template>

<script>
import { useQuasar } from 'quasar';
import { DateTime } from 'luxon';

export default {
  setup() {
    const q = useQuasar();
    return { q };
  },
  data() {
    return {
      columns: [
        {
          name: 'expand',
          label: '',
          align: 'left',
          field: 'expand',
        },
        {
          name: 'name',
          label: 'Name',
          align: 'left',
          field: 'name',
          sortable: true,
        },
        {
          name: 'uploadedAt',
          label: 'Uploaded At',
          align: 'left',
          field: 'uploadedAt',
          sortable: true,
        },
        {
          name: 'actions',
          label: '',
          align: 'left',
          field: 'act',
          sortable: true,
        },
      ],
      latestStates: [],
      historyForSpecificState: [],
      expanded: [],
    };
  },
  mounted() {
    this.getLatest();
  },
  methods: {
    deleteState(row) {
      this.q.dialog({
        title: 'Confirm',
        message: `Delete '${row.name}' uploaded ${this.formatDateRelative(row.uploadedAt)}?`,
        ok: {
          push: true,
          label: 'Delete State',
          tabindex: 1,
        },
        cancel: {
          push: true,
          color: 'negative',
          label: 'Cancel',
          tabindex: 0,
        },
        persistent: true,
      }).onOk(() => {
        this.$api.delete(`/states/${row.id}`)
          .then(() => this.getHistoryForName(row.name));
      });
    },
    formatDateRelative(val) {
      return DateTime.fromMillis(val * 1000).toRelative();
    },
    formatDateISO(val) {
      return DateTime.fromMillis(val * 1000).toISO();
    },
    getHistoryForName(name) {
      this.$api.get(`/states/${name}/history`)
        .then((response) => {
          this.historyForSpecificState = response.data;
        });
    },
    async getHistoryForNameExpand(props) {
      await this.getHistoryForName(props.row.name);
      this.expanded = this.expanded[0] === props.row.name ? [] : [props.row.name];
    },
    getLatest() {
      this.$api.get('/states/latest')
        .then((response) => {
          this.latestStates = response.data;
        });
    },
  },
};
</script>
