<template>
  <div class="q-pa-md row">
    <q-card class="col-auto">
      <q-card-section>
        <div class="text-h6">Terraform States</div>
      </q-card-section>
      <q-table :rows="terraformStates" :columns="terraformStateCols" row-key="name">
        <template v-slot:body-cell-uploadedAt="props">
          <q-td :props="props">
            <span>
              {{ this.formatDateRelative(props.row.uploadedAt) }}
              <q-tooltip>
                {{ this.formatDateISO(props.row.uploadedAt) }}
              </q-tooltip>
            </span>
          </q-td>
        </template>

        <template v-slot:body-cell-actions="props">
          <q-td :props="props">
            <q-btn-dropdown
              split
              class="glossy q-mr-sm"
              size="sm"
              color="teal"
              icon="difference"
              @click="displayLatestDiff(props.row.name)"
            >
              <q-list dense>
                <q-item clickable v-close-popup @click="displayLatestDiff(props.row.name)">
                  <q-item-section avatar>
                    <q-avatar icon="difference" />
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>Show Latest Changes</q-item-label>
                  </q-item-section>
                </q-item>

                <q-item clickable v-close-popup @click="showHistory(props.row.name)">
                  <q-item-section avatar>
                    <q-avatar icon="history" />
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>List Update History</q-item-label>
                  </q-item-section>
                </q-item>

                <q-item clickable v-close-popup @click="visualizeState(props.row.name)">
                  <q-item-section avatar>
                    <q-avatar icon="account_tree" />
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>Visualize Current State</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-btn-dropdown>

            <q-btn-dropdown
              split
              class="glossy"
              size="sm"
              color="teal"
              icon="download"
              @click="downloadState(props.row.name)"
            >
              <q-list dense>
                <q-item clickable v-close-popup @click="downloadState(props.row.name)">
                  <q-item-section avatar>
                    <q-avatar icon="download" />
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>Download Current State</q-item-label>
                  </q-item-section>
                </q-item>

                <q-item clickable v-close-popup @click="viewState(props.row.name)">
                  <q-item-section avatar>
                    <q-avatar icon="preview" />
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>View Current State</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-btn-dropdown>
          </q-td>
        </template>
      </q-table>
    </q-card>

    <q-dialog v-model="changes">
      <q-card>
        <q-card-section>
          <div class="text-h6">What changed?</div>
        </q-card-section>

        <q-separator />

        <q-card-section style="max-height: 50vh" class="scroll">
          <q-list>
            <q-item v-for="(diff, field) in diffChanges" v-bind:key="field">
              <q-item-section>
                <q-item-label overline>{{ field }}</q-item-label>
                <q-item-label>
                  <span :class="classForDiffLine(diff)">
                    {{ formatDiff(diff) }}
                    <q-tooltip :class="classForTooltipForDiffLine(diff)">
                      {{ tooltipForDiffLine(diff) }}
                    </q-tooltip>
                  </span>
                </q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right">
          <q-btn flat label="Close" color="primary" v-close-popup />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <q-dialog v-model="showHistoryDialog">
      <q-card>
        <q-card-section>
          <div class="text-h6">State History</div>
        </q-card-section>

        <q-separator />

        <q-card-section style="max-height: 50vh" class="scroll">
          <q-table
            :columns="terraformStateCols.slice(0, -1)"
            :rows="stateHistoryData"
            :row-key="name">

            <template v-slot:body-cell-uploadedAt="props">
              <q-td :props="props">
            <span>
              {{ this.formatDateRelative(props.row.uploadedAt) }}
              <q-tooltip>
                {{ this.formatDateISO(props.row.uploadedAt) }}
              </q-tooltip>
            </span>
              </q-td>
            </template>
          </q-table>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right">
          <q-btn flat label="Close" color="primary" v-close-popup />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <q-dialog v-model="viewStateDialog">
      <q-card>
        <q-card-section>
          <div class="text-h6">Current State File</div>
        </q-card-section>

        <q-separator />

        <q-card-section style="max-height: 50vh" class="scroll">
          <pre>{{ currentStateContents }}</pre>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right">
          <q-btn flat label="Close" color="primary" v-close-popup />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script>
import { defineComponent, ref } from 'vue';
import { DateTime } from 'luxon';

export default defineComponent({
  name: 'IndexPage',
  methods: {
    formatDateRelative(val) {
      return DateTime.fromMillis(val * 1000).toRelative();
    },
    formatDateISO(val) {
      return DateTime.fromMillis(val * 1000).toISO();
    },

    // TODO: I would like to reduce the amount of methods here to detect new vs removed.
    //       There are currently 4 methods for styling and formatting.
    classForDiffLine(diff) {
      if (diff[0] === null) {
        return 'text-weight-bolder text-green-5';
      }

      if (diff[1] === null) {
        return 'text-weight-bolder text-red-10';
      }

      return '';
    },
    formatDiff(diff) {
      if (diff[0] === null) {
        return `+ ${diff[1]}`;
      }

      if (diff[1] === null) {
        return `-  ${diff[0]}`;
      }

      return `${diff[0]} ---> ${diff[1]}`;
    },
    tooltipForDiffLine(diff) {
      if (diff[0] === null) {
        return 'Field has been added';
      }

      if (diff[1] === null) {
        return 'Field has been removed';
      }

      return 'Field has been changed';
    },
    classForTooltipForDiffLine(diff) {
      if (diff[0] === null) {
        return 'bg-green-5';
      }

      if (diff[1] === null) {
        return 'bg-red-10';
      }

      return '';
    },

    retrieveTerraformStates() {
      this.$api.get('/states/latest')
        .then((response) => {
          this.terraformStates = response.data;
        });
    },
    displayLatestDiff(stateName) {
      this.$api.get(`/states/${stateName}/latest/diff`)
        .then((response) => {
          const unordered = response.data;
          this.diffChanges = Object.keys(unordered).sort().reduce(
            (obj, key) => {
              obj[key] = unordered[key];
              return obj;
            },
            {},
          );
          this.changes = true;
        });
    },
    showHistory(stateName) {
      this.$api.get(`/states/${stateName}/history`)
        .then((response) => {
          this.stateHistoryData = response.data;
          this.showHistoryDialog = true;
        });
    },
    visualizeState(stateName) {
      this.$q.notify({
        message: `Visualization for ${stateName} coming soon!`,
        type: 'info',
      });
    },
    downloadState(stateName) {
      this.$api.get(`/state/${stateName}`)
        .then((response) => {
          const tempLink = document.createElement('a');
          const stateBlob = new Blob([JSON.stringify(response.data, null, 2)], { type: 'text/plain' });

          tempLink.setAttribute('href', URL.createObjectURL(stateBlob));
          tempLink.setAttribute('download', `${stateName}.tfstate`);
          tempLink.click();

          URL.revokeObjectURL(tempLink.href);
        });
    },
    viewState(stateName) {
      this.$api.get(`/state/${stateName}`)
        .then((response) => {
          this.viewStateDialog = true;
          this.currentStateContents = response.data;
        });
    },
  },
  setup() {
    return {
      terraformStates: ref([]),
      changes: ref(false),
      diffChanges: ref(null),
      showHistoryDialog: ref(null),
      stateHistoryData: ref([]),
      viewStateDialog: ref(false),
      currentStateContents: ref(''),
    };
  },
  data() {
    return {
      terraformStateCols: [
        {
          name: 'name',
          label: 'Terraform Config Name',
          field: 'name',
          align: 'left',
        },
        {
          name: 'uploadedAt',
          label: 'Last Updated At',
          field: 'uploadedAt',
        },
        {
          name: 'updatedBy',
          label: 'Last Updated By',
          field: 'updatedBy',
        },
        {
          name: 'actions',
          label: 'Actions',
          align: 'center',
        },
      ],
    };
  },
  mounted() {
    this.retrieveTerraformStates();
  },
});
</script>
